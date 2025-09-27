package com.thirtyseventyc.gpian20;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.gpian20.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class fragChat extends Fragment {

    private Context context;

    private EditText userinput;
    private ImageButton sendbutton;
    private RecyclerView recyclerView;
    private messageCardAdapter MessageCardAdapter;

    private final List<messageCardModel> cards = new ArrayList<>();
    private final List<String> allUserNames = new ArrayList<>();

    private FirebaseDatabase database;
    private DatabaseReference messageRef;
    private DatabaseReference messageCountRef;

    private long messageCount = 0;
    private final String username = "samay";

    public static String sendername = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();
        database = FirebaseDatabase.getInstance();
        messageRef = database.getReference("messages");
        messageCountRef = database.getReference("messageCount/count");

        allUserNames.add("null");

        setupMessageCountListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_chat, container, false);

        userinput = view.findViewById(R.id.userinput);
        sendbutton = view.findViewById(R.id.sendbutton);
        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        MessageCardAdapter = new messageCardAdapter(cards, context);
        recyclerView.setAdapter(MessageCardAdapter);

        sendbutton.setOnClickListener(v -> sendMessage());

        getCards();

        return view;
    }

    private void setupMessageCountListener() {
        messageCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long count = snapshot.getValue(Long.class);
                if (count != null) {
                    messageCount = count;
                    loadNewMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log or handle error if needed
            }
        });
    }

    private void loadNewMessages() {
        messageRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                long newMessageCount = task.getResult().getChildrenCount();
                if (newMessageCount > messageCount) {
                    for (long i = messageCount + 1; i <= newMessageCount; i++) {
                        String messageChild = "message" + i;
                        DataSnapshot messageSnapshot = task.getResult().child(messageChild);
                        String message = messageSnapshot.child("message").getValue(String.class);
                        String sender = messageSnapshot.child("sender").getValue(String.class);
                        String time = String.valueOf(messageSnapshot.child("time").getValue());

                        if (sender != null && !sender.equals(username) && message != null) {
                            cards.add(new messageCardModel(sender, message, time));
                            MessageCardAdapter.notifyItemInserted(cards.size() - 1);
                            recyclerView.smoothScrollToPosition(cards.size() - 1);
                        }
                    }
                    messageCount = newMessageCount;
                    messageCountRef.setValue(messageCount);
                }
            }
        });
    }

    private void getCards() {
        messageRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                messageCount = task.getResult().getChildrenCount();

                for (int i = 1; i <= messageCount; i++) {
                    String messageChild = "message" + i;
                    DataSnapshot messageSnapshot = task.getResult().child(messageChild);
                    String message = messageSnapshot.child("message").getValue(String.class);
                    String sender = messageSnapshot.child("sender").getValue(String.class);
                    String time = String.valueOf(messageSnapshot.child("time").getValue());

                    if (message != null && !message.trim().isEmpty() && sender != null) {
                        allUserNames.add(sender);
                        cards.add(new messageCardModel(sender, message, time));
                    }
                }
                MessageCardAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(cards.size() - 1);
                MessageCardAdapter.getUsername(allUserNames);
            }
        });
    }

    private void sendMessage() {
        messageCount++;
        String messageText = userinput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(context, "Cannot send empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        String messagePath = "messages/message" + messageCount;

        DatabaseReference messageTextRef = database.getReference(messagePath + "/message");
        DatabaseReference senderRef = database.getReference(messagePath + "/sender");
        DatabaseReference timeRef = database.getReference(messagePath + "/time");

        String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());

        messageTextRef.setValue(messageText);
        senderRef.setValue(username);
        timeRef.setValue(time);
        messageCountRef.setValue(messageCount);

        Toast.makeText(context, String.valueOf(messageCount), Toast.LENGTH_SHORT).show();
        userinput.setText("");

        cards.add(new messageCardModel(username, messageText, time));
        MessageCardAdapter.notifyItemInserted(cards.size() - 1);
        recyclerView.scrollToPosition(cards.size() - 1);

        sendername = username;
    }
}
