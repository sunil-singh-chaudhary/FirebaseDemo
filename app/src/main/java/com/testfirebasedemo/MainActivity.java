package com.testfirebasedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtDetails;
    private EditText inputName, inputEmail;
    private Button btnSave;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private String userId;
    List<User> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        // Displaying toolbar icon
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setIcon( R.mipmap.ic_launcher );
        txtDetails = findViewById( R.id.txt_user );
        inputName = findViewById( R.id.name );
        inputEmail = findViewById( R.id.email );
        btnSave = findViewById( R.id.btn_save );

        mFirebaseInstance = FirebaseDatabase.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference( "users" );



       mFirebaseInstance.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<User> list = new ArrayList<>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    list.add(child.getValue(User.class));
                    list.size();
                    Log.e( "SIZE-",list.size()+"" );
                }
                for (int i=0;i<list.size();i++){
                    Log.e( "email-",list.get( i).getEmail()+"" );
                }



            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
         });

        // Save / update the user
        btnSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String email = inputEmail.getText().toString();
                createUser( name, email );
            }
        } );

        toggleButton();
    }

    // Changing button text
    private void toggleButton() {
        if (TextUtils.isEmpty( userId )) {
            btnSave.setText( "Save" );
        } else {
            btnSave.setText( "Update" );
        }
    }

    /**
     * Creating new user node under 'users'
     */
    private void createUser(String name, String email) {
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference( "users" );
        userId = mFirebaseDatabase.push().getKey();
        User user = new User( name, email );
        mFirebaseDatabase.child( userId ).setValue( user );
        addUserChangeListener();
    }

    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child( userId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue( User.class );

                // Check for null
                if (user == null) {
                    Log.e( TAG, "User data is null!" );
                    return;
                }

                Log.e( TAG, "User data is changed!" + user.name + ", " + user.email );

                // Display newly updated name and email
                txtDetails.setText( user.name + ", " + user.email );

                // clear edit text
                inputEmail.setText( "" );
                inputName.setText( "" );

                toggleButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e( TAG, "Failed to read user", error.toException() );
            }
        } );
    }

    private void updateUser(String name, String email) {
        // updating the user via child nodes
        if (!TextUtils.isEmpty( name ))
            mFirebaseDatabase.child( userId ).child( "name" ).setValue( name );

        if (!TextUtils.isEmpty( email ))
            mFirebaseDatabase.child( userId ).child( "email" ).setValue( email );
    }
}