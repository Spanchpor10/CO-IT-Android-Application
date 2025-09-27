package com.thirtyseventyc.gpian20;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.gpian20.R;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginUser extends AppCompatActivity {
    private static final String TAG = "loginUser";

    private EditText userMail;
    private EditText userPass;
    private EditText vCode;

    private RadioButton isTeacher;
    private RadioButton isStudent;
    private CardView signUserUp;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);
        statusbarcolor();

        userMail = findViewById(R.id.studentEmailSignin);
        userPass = findViewById(R.id.studentPassSignin);
        vCode = findViewById(R.id.secCode);
        isTeacher = findViewById(R.id.redTeacher);
        isStudent = findViewById(R.id.redStudent);
        signUserUp = findViewById(R.id.saveEdt);

        mAuth = FirebaseAuth.getInstance();

        signUserUp.setOnClickListener(v -> signTheUser());
    }

    private void statusbarcolor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.lighyB, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.lighyB));
        }
    }

    private void signTheUser() {
        String userEmail = userMail.getText().toString().trim();
        String userPassword = userPass.getText().toString();
        String verificationCode = vCode.getText().toString();

        if (isTeacher.isChecked()) {
            if ("aa".equals(verificationCode)) {
                mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent toInitial = new Intent(getApplicationContext(), Initila_Teacher.class);
                                toInitial.putExtra("isTeacher", "yes");
                                startActivity(toInitial);
                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(loginUser.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getApplicationContext(), "Please Enter Valid Code", Toast.LENGTH_SHORT).show();
            }
        } else if (isStudent.isChecked()) {
            mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent toInitial = new Intent(getApplicationContext(), Initial.class);
                            startActivity(toInitial);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(loginUser.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please Select the Position", Toast.LENGTH_SHORT).show();
        }
    }
}
