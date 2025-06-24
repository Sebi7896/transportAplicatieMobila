package com.sebastian.licentafrontendtransport.Login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;

import com.sebastian.licentafrontendtransport.Cards.UrbisPassCard.CardsAdapter;
import com.sebastian.licentafrontendtransport.Cards.UrbisPassCard.PayWithNewCardActivity;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Payment.Card;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.MemberDelete;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbisPassCard;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbisPassCardMember;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbissCardAPI;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.stripe.android.core.exception.APIConnectionException;
import com.stripe.android.core.exception.APIException;
import com.stripe.android.core.exception.AuthenticationException;
import com.stripe.android.core.exception.InvalidRequestException;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MembershipCardsAdapter extends RecyclerView.Adapter<MembershipCardsAdapter.MemberViewHolder> {

    private final Context context;
    private final List<UrbisPassCardMember> memberships;
    private int currentPosition = -1;
    private final ActivityResultLauncher<Intent> rechargeLauncher;

    public MembershipCardsAdapter(Context context, List<UrbisPassCardMember> memberships, ActivityResultLauncher<Intent> rechargeLauncher) {
        this.context = context;
        this.memberships = memberships;
        this.rechargeLauncher = rechargeLauncher;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_membership_card, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        UrbisPassCardMember member = memberships.get(position);
        holder.statusText.setText(String.format("Status: %s", member.getStatus()));
        String status = member.getStatus().toLowerCase();
        switch (status) {
            case "active":
                holder.statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                break;
            case "non-active":
                holder.statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                break;
            case "pending":
                holder.statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                break;
            default:
                holder.statusText.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                break;
        }
        holder.fullNameText.setText(member.getPerson().getFullName());
        holder.studentCheckBox.setChecked(member.getPerson().isStudent());
    }

    @Override
    public int getItemCount() {
        return memberships.size();
    }

    public void removeCard(int position) {
        UrbisPassCardMember removed = memberships.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "Removed membership for: " + removed.getPerson().getFullName(), Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this membership?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMembershipFromServer(position))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    notifyItemChanged(position);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void rechargeMembership(int position) {
        fetchBankCardsForRecharge(position);
    }

    private void fetchBankCardsForRecharge(int position) {
        LoginManager loginManager = new LoginManager(context);
        Retrofit client = RetrofitClient.getInstance(context);
        PaymentApi api = client.create(PaymentApi.class);

        Call<List<Card>> call = api.getCards("Bearer " + loginManager.getAccessToken());
        call.enqueue(new AuthCallBack<List<Card>>(context, loginManager,
                () -> api.getCards("Bearer " + loginManager.getAccessToken())) {
            @Override
            public void onSucces(List<Card> cards) {
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_recharge_membership, null);

                Spinner spinnerDuration = dialogView.findViewById(R.id.spinner_duration);
                Spinner spinnerCard = dialogView.findViewById(R.id.spinner_card);
                currentPosition = position;
                showRechargeDialog(dialogView, spinnerCard, spinnerDuration, cards);

            }

            @Override
            public void onError(Response<List<Card>> response) {
                Toast.makeText(context, "Error loading cards", Toast.LENGTH_SHORT).show();
                notifyItemChanged(position);
            }
        });
    }

    private void processRecharge(int position, Card selectedCard, String selectedDuration) {
        String durationString = selectedDuration.substring(0, selectedDuration.indexOf("->")).trim();
        if ("Non existing card".equals(selectedCard.getBrand())) {
            Intent intent = new Intent(context, PayWithNewCardActivity.class);
            //no name
            UrbisPassCard urbisPassCard = new UrbisPassCard("",
                    memberships.get(currentPosition).getCnp(),
                    Integer.parseInt(durationString),
                    memberships.get(currentPosition).getPerson().isStudent(),
                    selectedCard, false);
            intent.putExtra("membership", urbisPassCard);
            rechargeLauncher.launch(intent);
            notifyItemChanged(position);
            return;
        }

        UrbisPassCard urbisPassCard = new UrbisPassCard("", memberships.get(currentPosition).getCnp(), Integer.parseInt(durationString), memberships.get(currentPosition).getPerson().isStudent(), selectedCard, false);
        sendCardToBackend(urbisPassCard);
    }

    private void sendCardToBackend(UrbisPassCard urbisPassCard) {
        LoginManager loginManager = new LoginManager(context);
        Retrofit client = RetrofitClient.getInstance(context);
        UrbissCardAPI urbisCardAPI = client.create(UrbissCardAPI.class);
        Call<MemberDelete> call = urbisCardAPI.updateMembership(
            "Bearer " + loginManager.getAccessToken(),
            urbisPassCard
        );
        call.enqueue(new AuthCallBack<MemberDelete>(context, loginManager,
            () -> urbisCardAPI.updateMembership("Bearer " + loginManager.getAccessToken(), urbisPassCard)) {
            @Override
            public void onSucces(MemberDelete responseBody) {
                Toast.makeText(context, responseBody.getMessage(), Toast.LENGTH_SHORT).show();
                if (memberships.get(currentPosition).getPerson().isStudent()) {
                    memberships.get(currentPosition).setStatus("pending");
                } else {
                    memberships.get(currentPosition).setStatus("active");
                }
                notifyItemChanged(currentPosition);
            }
            @Override
            public void onError(Response<MemberDelete> response) {
                super.onError(response);
                Toast.makeText(context, "Urbispass update failed!", Toast.LENGTH_SHORT).show();
                notifyItemChanged(currentPosition);
            }
        });
    }

    private void deleteMembershipFromServer(int position) {
        LoginManager loginManager = new LoginManager(context);
        String accessToken = loginManager.getAccessToken();
        UrbissCardAPI api = RetrofitClient.getInstance(context).create(UrbissCardAPI.class);

        String cardId = memberships.get(position).getCnp();

        api.deleteMembership("Bearer " + accessToken, cardId).enqueue(new AuthCallBack<MemberDelete>(
                context, loginManager, () -> api.deleteMembership("Bearer " + loginManager.getRefreshToken(), cardId)
        ) {
            @Override
            public void onSucces(MemberDelete responseBody) throws APIConnectionException, APIException, AuthenticationException, InvalidRequestException {
                removeCard(position);
            }

            @Override
            public void onError(Response<MemberDelete> response) {
                super.onError(response);
                notifyItemChanged(position);
                Toast.makeText(context, "Failed to delete membership!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<MemberDelete> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                notifyItemChanged(position);
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public ItemTouchHelper getSwipeHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                currentPosition = viewHolder.getBindingAdapterPosition();
                int position = currentPosition;
                if (position == RecyclerView.NO_POSITION) return;

                UrbisPassCardMember member = memberships.get(position);
                String status = member.getStatus().toLowerCase();

                if ("non-active".equals(status)) {
                    if (direction == ItemTouchHelper.RIGHT) {
                        confirmDelete(position);
                    } else if (direction == ItemTouchHelper.LEFT) {
                        rechargeMembership(position);
                    }
                } else {
                    notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState,
                                    boolean isCurrentlyActive) {
                android.view.View itemView = viewHolder.itemView;
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    return;
                }

                UrbisPassCardMember member = memberships.get(position);
                String status = member.getStatus().toLowerCase();

                if (!"non-active".equals(status)) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    return;
                }

                android.graphics.Paint paint = new android.graphics.Paint();
                android.graphics.Paint textPaint = new android.graphics.Paint();
                textPaint.setColor(android.graphics.Color.WHITE);
                textPaint.setTextSize(48f);
                textPaint.setTextAlign(android.graphics.Paint.Align.LEFT);
                float textY = itemView.getTop() + (itemView.getHeight() / 2f) + 16f;

                if (dX > 0) {
                    paint.setColor(android.graphics.Color.RED);
                    android.graphics.RectF background = new android.graphics.RectF(
                            itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + dX, itemView.getBottom()
                    );
                    c.drawRect(background, paint);
                    c.drawText("Delete", itemView.getLeft() + 50f, textY, textPaint);
                } else if (dX < 0) {
                    paint.setColor(android.graphics.Color.parseColor("#388E3C"));
                    android.graphics.RectF background = new android.graphics.RectF(
                            itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom()
                    );
                    c.drawRect(background, paint);
                    textPaint.setTextAlign(android.graphics.Paint.Align.RIGHT);
                    c.drawText("Recharge", itemView.getRight() - 50f, textY, textPaint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView statusText;
        TextView fullNameText;
        android.widget.CheckBox studentCheckBox;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            statusText = itemView.findViewById(R.id.membership_status);
            fullNameText = itemView.findViewById(R.id.membership_full_name);
            studentCheckBox = itemView.findViewById(R.id.membership_is_student);
        }
    }

    private void setupCardSpinner(Spinner spinnerCard, List<Card> cards) {
        if (cards == null) {
            cards = new ArrayList<>();
        }
        Card addNewOption = new Card();
        addNewOption.setBrand("Non existing card");
        cards.add(addNewOption);

        CardsAdapter adapter = new CardsAdapter(context, cards);
        spinnerCard.setAdapter(adapter);
    }
    private void setupDurationSpinner(Spinner spinnerDuration) {
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.duration_array,
                android.R.layout.simple_spinner_item
        );
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(durationAdapter);
    }

    private void showRechargeDialog(View dialogView, Spinner spinnerCard, Spinner spinnerDuration, List<Card> cards) {
        setupDurationSpinner(spinnerDuration);
        setupCardSpinner(spinnerCard, cards);

        new AlertDialog.Builder(context)
                .setTitle("Recharge UrbisPass Card")
                .setView(dialogView)
                .setPositiveButton("Recharge", (dialog, which) -> {
                    Card selectedCard = (Card) spinnerCard.getSelectedItem();
                    String selectedDuration = spinnerDuration.getSelectedItem().toString();

                    if (memberships.get(currentPosition).getPerson().isStudent()) {
                        new AlertDialog.Builder(context)
                                .setTitle("Student verification")
                                .setMessage("As a student, your card will not be valid until verified by your faculty!")
                                .setPositiveButton("OK", (dialogInterface, i) -> processRecharge(currentPosition, selectedCard, selectedDuration))
                                .setNegativeButton("Cancel", (dialogInterface, i) -> notifyItemChanged(currentPosition))
                                .show();
                        return;
                    }

                    processRecharge(currentPosition, selectedCard, selectedDuration);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    notifyItemChanged(currentPosition);
                })
                .setCancelable(false)
                .show();
    }
}
