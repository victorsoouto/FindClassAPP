package com.findclass.ajvm.findclassapp.ScheduleFragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.findclass.ajvm.findclassapp.Adapter.MyScheduleStudentAdapter;
import com.findclass.ajvm.findclassapp.Helper.RecyclerItemClickListener;
import com.findclass.ajvm.findclassapp.Model.Date_Status;
import com.findclass.ajvm.findclassapp.Model.Date_Time;
import com.findclass.ajvm.findclassapp.Model.Schedule;
import com.findclass.ajvm.findclassapp.Model.ScheduleObject;
import com.findclass.ajvm.findclassapp.Model.Subject;
import com.findclass.ajvm.findclassapp.Model.Time;
import com.findclass.ajvm.findclassapp.Model.User;
import com.findclass.ajvm.findclassapp.R;
import com.findclass.ajvm.findclassapp.menuActivities.InfoScheduleStudentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyScheduleStudentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //Elemenetos do firebase
    private DatabaseReference schedulesRef;
    private DatabaseReference rootRef;
    private FirebaseAuth auth;
    //Elementos gráficos
    private RecyclerView recyclerViewMyScheduleList;
    private ProgressDialog progress;
    private SwipeRefreshLayout mSwipeToRefresh;
    //Elementos auxiliares
    private MyScheduleStudentAdapter adapter;
    private ArrayList<ScheduleObject> myScheduleObjects = new ArrayList<>();

    public MyScheduleStudentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_schedule_student, container, false);
        //setando atributos
        adapter = new MyScheduleStudentAdapter(myScheduleObjects);
        //setando atributos do firebase
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        schedulesRef = rootRef.child("schedule");
        //Setando atributos gráficos
        recyclerViewMyScheduleList = view.findViewById(R.id.recyclerViewMySchedule);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        recyclerViewMyScheduleList.setLayoutManager(layoutManager1);
        recyclerViewMyScheduleList.setHasFixedSize(true);
        recyclerViewMyScheduleList.setAdapter(adapter);
        mSwipeToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_container);
        mSwipeToRefresh.setOnRefreshListener(this);
        //Adição do evento de clique aos itens da lista
        recyclerViewMyScheduleList.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewMyScheduleList,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            //Definição da ação do clique.
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent intent = new Intent(getContext(),InfoScheduleStudentActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("schedule",myScheduleObjects.get(position));
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {
                                //
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                //
                            }
                        }
                )
        );

        return view;
    }

    //Método que define as ações que devem ser executadas ao iniciar o Fragment
    @Override
    public void onStart() {
        super.onStart();
        retrieveMySchedules();
    }

    //Método que define as ações que devem ser executadas ao abandonar o Fragment
    @Override
    public void onStop() {
        super.onStop();
    }

    //Método para buscar no banco de dados minha lista de aulas marcadas
    public void retrieveMySchedules(){
        progress = new ProgressDialog(getActivity());
        progress.setMessage("Carregando...");
        progress.show();

        myScheduleObjects.clear();

        final ArrayList<DataSnapshot> myScheduleSnapshots = new ArrayList<>();

        schedulesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                            for (DataSnapshot dataSnapshot2: dataSnapshot1.getChildren()){
                                if(dataSnapshot2.getKey().equals(auth.getCurrentUser().getUid())){
                                    for (DataSnapshot scheduleSnap: dataSnapshot2.getChildren()){
                                        if (scheduleSnap.child("finish").getValue(Integer.class).equals(0)) {
                                            myScheduleSnapshots.add(scheduleSnap);
                                        }
                                    }
                                }
                            }
                        }
                        for(DataSnapshot scheduleSnap: myScheduleSnapshots){
                            Schedule schedule = scheduleSnap.getValue(Schedule.class);
                            retrieveProfessor(schedule);
                        }
                        progress.dismiss();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                }
        );
    }

    //Método para buscar no banco de dados os professores das aulas marcadas
    public void retrieveProfessor(final Schedule schedule){
        DatabaseReference usersRef = rootRef.child("users");
        usersRef
                .child(schedule.getProfessor_id())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User professor = dataSnapshot.getValue(User.class);
                                retrieveStudent(schedule,professor);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //
                            }
                        }
                );
    }

    //Método para buscar no banco de dados o aluno das aulas marcadas
    public void retrieveStudent(final Schedule schedule, final User professor){
        DatabaseReference usersRef = rootRef.child("users");
        usersRef
                .child(schedule.getStudent_id())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User student = dataSnapshot.getValue(User.class);
                                retrieveSubject(schedule,professor,student);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //
                            }
                        }
                );
    }

    //Método para buscar no banco de dados as disciplinas das aulas
    public void retrieveSubject(final Schedule schedule, final User professor, final User student){
        DatabaseReference subjectsRef = rootRef.child("subjects");
        subjectsRef
                .child(schedule.getSubject_id())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Subject subject = dataSnapshot.getValue(Subject.class);
                                retrieveDatetime(schedule,professor,student,subject);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //
                            }
                        }
                );
    }

    //Método para buscar no banco de dados a data da aula
    public void retrieveDatetime(final Schedule schedule, final User professor, final User student, final Subject subject){
        final DatabaseReference datetimeRef = rootRef.child("availability");
        final DatabaseReference thisDatetimeRef = datetimeRef.child(schedule.getProfessor_id());
        thisDatetimeRef
                .child("dateTimes")
                .child(schedule.getDatetime_id())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Date_Time date_time = dataSnapshot.getValue(Date_Time.class);
                                retrieveDate(schedule,professor,student,subject,thisDatetimeRef,date_time);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //
                            }
                        }
                );
    }

    //Método para buscar no banco de dados a data da aula
    public void retrieveDate(final Schedule schedule, final User professor, final User student, final Subject subject, final DatabaseReference datetimeRef, final Date_Time date_time){
        datetimeRef
                .child("dates")
                .child(date_time.getDate_id())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Date_Status date = dataSnapshot.getValue(Date_Status.class);
                                retrieveTime(schedule,professor,student,subject,datetimeRef,date_time,date);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //
                            }
                        }
                );

    }

    //Método para buscar no banco de dados a hora da aula
    public void retrieveTime(final Schedule schedule, final User professor, final User student, final Subject subject, DatabaseReference datetimeRef, Date_Time date_time, final Date_Status date){
        datetimeRef
                .child("times")
                .child(date_time.getTime_id())
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Time time = dataSnapshot.getValue(Time.class);
                                finish(date,time,professor,student,schedule);
                                if(schedule.getFinish()==1){
                                    myScheduleObjects.clear();
                                    retrieveMySchedules();
                                }else{
                                    ScheduleObject scheduleObject = new ScheduleObject(professor, student, subject, time, date, schedule.getId(),schedule.getCancel());
                                    myScheduleObjects.add(scheduleObject);
                                    sortMySchedules();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //
                            }
                        }
                );
    }

    //Método para finalizar a aula automaticamente quando passar da data
    public void finish(Date_Status date,Time time,User professor,User student, Schedule schedule){
        try {
            //transformando string do banco de dados em Date
            String dateString = date.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            Date data = new Date();
            try {
                data = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String oldData = dateFormat.format(data)+"-"+time.getEndTime();
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yy-HH:mm");

            Date dataTime = new Date();
            try {
                dataTime = sdf2.parse(oldData);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //Pegando a data atual
            Date dataAtual = new Date();
            try {
                dataAtual = sdf.parse(String.valueOf(dataAtual));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (dataAtual.after(dataTime)) {
                schedule.setFinish(1);
                schedulesRef.child(professor.getId()).child(student.getId()).child(schedule.getId()).child("finish").setValue(1);
            }
        }
        catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Método de buscar aula por: professor, disciplina ou nível
    public void searchSchedule(String text) {
        List<ScheduleObject> listScheduleSearch = new ArrayList<>();
        for (ScheduleObject scheduleObject : myScheduleObjects) {
            String subject = treatText(scheduleObject.getSubject().getName());
            String professor = treatText(scheduleObject.getProfessor().getName());
            String level = treatText(scheduleObject.getSubject().getLevel());
            if (subject.contains(text)||professor.contains(text)||level.contains(text)) {
                listScheduleSearch.add(scheduleObject);
            }
        }
        adapter = new MyScheduleStudentAdapter(listScheduleSearch);
        recyclerViewMyScheduleList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //Método para tratar strings acentuadas e com letras maiúsculas
    public static String treatText(String text){
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }

    //Método para recarregar lista completa de aulas marcadas
    public void reloadList() {
        adapter = new MyScheduleStudentAdapter(myScheduleObjects);
        recyclerViewMyScheduleList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //Método para atualizar a lista
    @Override
    public void onRefresh() {
        retrieveMySchedules();
        mSwipeToRefresh.setRefreshing(false);
    }

    //Método para ordenar a lista por data
    public void sortMySchedules(){
        Collections.sort(myScheduleObjects);
        ArrayList<ScheduleObject> canceledScheduleObjects = new ArrayList<>();
        ArrayList<ScheduleObject> notCanceledScheduleObjects = new ArrayList<>();
        for (ScheduleObject scheduleObject : myScheduleObjects){
            if (scheduleObject.getCancel() == 1){
                canceledScheduleObjects.add(scheduleObject);
            }
            else{
                notCanceledScheduleObjects.add(scheduleObject);
            }
        }
        myScheduleObjects.clear();
        myScheduleObjects.addAll(notCanceledScheduleObjects);
        myScheduleObjects.addAll(canceledScheduleObjects);
        adapter.notifyDataSetChanged();
    }

}
