package PME.myfitnessbuddy.storage;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;


import PME.myfitnessbuddy.model.ExerciseMuscleGroupCrossRef;
import PME.myfitnessbuddy.model.MuscleGroup;
import PME.myfitnessbuddy.model.Person;
import PME.myfitnessbuddy.model.training.Category;


import PME.myfitnessbuddy.model.training.CategoryConverter;
import PME.myfitnessbuddy.model.training.Training;
import PME.myfitnessbuddy.model.exercise.Exercise;
import com.github.javafaker.Faker;
import com.myfitnessbuddy.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database( entities = {Person.class, Training.class, MuscleGroup.class, Exercise.class, ExerciseMuscleGroupCrossRef.class}, version = 26     )
@TypeConverters({CategoryConverter.class})
public abstract class MyFitnessBuddyDatabase extends RoomDatabase {

    private static final String LOG_TAG_DB = "MyFitnessBuddyDB";

    /*
        Contact DAO reference, will be filled by Android
     */
    public abstract PersonDao personDao();
    public abstract TrainingDao trainingDao();
  //  public abstract MuscleGroupDao muscleGroupDao();
    public abstract ExerciseDao exerciseDao();

    public abstract MuscleGroupDao muscleGroupDao();

    public abstract ExerciseCrossRefDao exerciseMuscleGroupCrossRefDao();


    /*
        Executor service to perform database operations asynchronous and independent from UI thread
     */
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool( NUMBER_OF_THREADS );

    /*
        Singleton Instance
     */
    private static volatile MyFitnessBuddyDatabase INSTANCE;

    /*
        Helper methods to ease external usage of ExecutorService
        e.g. perform async database operations
     */
    public static <T> T executeWithReturn(Callable<T> task)
            throws ExecutionException, InterruptedException
    {
        return databaseWriteExecutor.invokeAny( Collections.singletonList( task ) );
    }

    public static void execute( Runnable runnable )
    {
        databaseWriteExecutor.execute( runnable );
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
        Singleton 'getInstance' method to create database instance thereby opening and, if not
        already done, init the database. Note the 'createCallback'.
     */
    static MyFitnessBuddyDatabase getDatabase(final Context context) {
        Log.i( LOG_TAG_DB, "getDatabase() called" );
        if (INSTANCE == null) {
            synchronized (MyFitnessBuddyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MyFitnessBuddyDatabase.class, "myfitnessbuddy_db")
                            .addCallback(createCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /*
        Create DB Callback
        Used to add some initial data
     */
    private static RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Log.i( LOG_TAG_DB, "onCreate() called" );

            execute(() -> {
               PersonDao dao = INSTANCE.personDao();
               dao.deleteAll();

               TrainingDao daoTraining = INSTANCE.trainingDao();
               daoTraining.deleteAll();

                ExerciseDao daoExercise = INSTANCE.exerciseDao();
                daoExercise.deleteAll();

                ExerciseCrossRefDao exerciseCrossRefDao = INSTANCE.exerciseMuscleGroupCrossRefDao();
                exerciseCrossRefDao.deleteAll();



                MuscleGroupDao daoMuscleGroup = INSTANCE.muscleGroupDao();

                daoMuscleGroup.deleteAll();



                Faker faker = Faker.instance();
                for (int i = 0; i < 40; i++)
                {
                   Training training = new Training(faker.team().sport(), Category.category1);


                    training.setCreated( System.currentTimeMillis() );
                    training.setProfileImageUrl( faker.avatar().image() );
                    training.setModified( training.getCreated() );
                    training.setVersion( 1 );
                    daoTraining.insert(training);
/*
                    MuscleGroup muscleGroup = new MuscleGroup(faker.chuckNorris().fact());
                    muscleGroup.setCreated( System.currentTimeMillis() );
                    muscleGroup.setProfileImageUrlByString( "test" );
                    muscleGroup.setModified( muscleGroup.getCreated() );
                    muscleGroup.setVersion( 1 );


                    Exercise exercise = new Exercise(faker.pokemon().name(),faker.pokemon().location());
                    exercise.setCreated( System.currentTimeMillis() );
                    //exercise.setProfileImageUrl( faker.avatar().image() );
                    exercise.setModified( training.getCreated() );
                    exercise.setVersion( 1 );

                    long exerciseId = daoExercise.insertExercise(exercise);
                    long muscleGroupId = daoMuscleGroup.insertMuscleGroup(muscleGroup);

                    ExerciseMuscleGroupCrossRef exerciseMuscleGroupCrossRef = new ExerciseMuscleGroupCrossRef(exerciseId, muscleGroupId);
                    exerciseMuscleGroupCrossRef.setCreated( System.currentTimeMillis() );
                    //exercise.setProfileImageUrl( faker.avatar().image() );
                    exerciseMuscleGroupCrossRef.setModified( training.getCreated() );
                    exerciseMuscleGroupCrossRef.setVersion( 1 );
                    exerciseCrossRefDao.insertExerciseCrossRef(exerciseMuscleGroupCrossRef);
*/




                    Log.i(LOG_TAG_DB, "Inserted 10 values to DB");
                }
                MuscleGroup muscleGroup2 = new MuscleGroup("Beine");
                muscleGroup2.setCreated( System.currentTimeMillis() );
                muscleGroup2.setProfileImageUrlByString( muscleGroup2.getDesignation() );
                muscleGroup2.setModified( muscleGroup2.getCreated() );
                muscleGroup2.setVersion( 1 );
                daoMuscleGroup.insertMuscleGroup(muscleGroup2);


                MuscleGroup muscleGroup3 = new MuscleGroup("Brust");
                muscleGroup3.setCreated( System.currentTimeMillis() );
                muscleGroup3.setProfileImageUrlByString( "Brust" );
                muscleGroup3.setModified( muscleGroup3.getCreated() );
                muscleGroup3.setVersion( 1 );
                daoMuscleGroup.insertMuscleGroup(muscleGroup3);


                MuscleGroup muscleGroup4 = new MuscleGroup("Rücken");
                muscleGroup4.setCreated( System.currentTimeMillis() );
                muscleGroup4.setProfileImageUrlByString( "Rücken" );
                muscleGroup4.setModified( muscleGroup4.getCreated() );
                muscleGroup4.setVersion( 1 );
                daoMuscleGroup.insertMuscleGroup(muscleGroup4);


                MuscleGroup muscleGroup5 = new MuscleGroup("Bizeps");
                muscleGroup5.setCreated( System.currentTimeMillis() );
                muscleGroup5.setProfileImageUrlByString( "Bizeps" );
                muscleGroup5.setModified( muscleGroup5.getCreated() );
                muscleGroup5.setVersion( 1 );
                daoMuscleGroup.insertMuscleGroup(muscleGroup5);



                Log.i(LOG_TAG_DB, "Inserted 10 values to DB");
            });
        }
    };

}
