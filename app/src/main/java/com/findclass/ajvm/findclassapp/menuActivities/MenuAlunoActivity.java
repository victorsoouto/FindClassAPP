package com.findclass.ajvm.findclassapp.menuActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.findclass.ajvm.findclassapp.AccountActivities.SignInActivity;
import com.findclass.ajvm.findclassapp.AccountActivities.UpdateDataActivity;
import com.findclass.ajvm.findclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MenuAlunoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_aluno);
        FirebaseDatabase dbRef = FirebaseDatabase.getInstance();

        try {
            dbRef.getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (auth.getCurrentUser() != null) {
                        if (!dataSnapshot.hasChild(auth.getCurrentUser().getUid().toString())) {
                            auth.signOut();
                        }
                    }
                }

                public void onCancelled(DatabaseError databaseError) {
                    //code
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView email = header.findViewById(R.id.textViewEmailAluno);
        email.setText(auth.getCurrentUser().getEmail());

        FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid()).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            String name = dataSnapshot.child("name")
                                    .getValue(String.class);
                            String surname = dataSnapshot.child("surname")
                                    .getValue(String.class);

                            NavigationView navigationView = findViewById(R.id.nav_view);

                            View header = navigationView.getHeaderView(0);
                            TextView textViewAlunoName = header.findViewById(R.id.textViewNameAluno);
                            textViewAlunoName.setText(name + " " + surname);
                        } catch (Exception e) {
                            Toast.makeText(MenuAlunoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //code
                    }
                });
        //abas
       /* final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Aulas Marcadas",)//Colocar activity notificacoes
                .create()
        );
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_aluno, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logoutAluno) {
            logout(this.findViewById(R.id.toolbar));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_editAccountAluno) {
            Intent intent = new Intent(getBaseContext(), UpdateDataActivity.class);
            startActivity(intent);
        }else if(id == R.id.nav_LevelFundamental){
            Intent intent = new Intent(getBaseContext(), SubjectCategoryFundamentalActivity.class);
            startActivity(intent);
        }else if(id == R.id.nav_LevelMedio){
            Intent intent = new Intent(getBaseContext(), SubjectCategoryMedioActivity.class);
            startActivity(intent);
        }else if(id == R.id.nav_LevelVariados){
            Intent intent = new Intent(getBaseContext(), SubjectCategoryVariadosActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout(View view){
        try{
            FirebaseAuth.getInstance().signOut();
        }catch (Exception e){
            String message = "Erro, você já está delogado";
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "Saiu!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MenuAlunoActivity.this,SignInActivity.class));
        finish();
    }

}