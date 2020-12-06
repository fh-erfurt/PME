package com.example.myfitnessbuddy.storage;

import android.app.Application;
import android.content.Context;


import com.example.myfitnessbuddy.model.Category;
import com.example.myfitnessbuddy.model.Training;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class TrainingRepository {
    public static final String LOG_TAG = "TrainingRepository";

    private TrainingDao trainingDao;

    private static TrainingRepository INSTANCE;

    public static TrainingRepository getRepository( Application application )
    {
        if( INSTANCE == null ) {
            synchronized ( TrainingRepository.class ) {
                if( INSTANCE == null ) {
                    INSTANCE = new TrainingRepository( application );
                }
            }
        }

        return INSTANCE;
    }

    public TrainingRepository(Context context) {
        MyFitnessBuddyDatabase db = MyFitnessBuddyDatabase.getDatabase( context );
        this.trainingDao = db.trainingDao();
    }

    public List<Training> getTrainings()
    {
        return this.query( () -> this.trainingDao.getTrainings() );
    }

    public List<Training> getTrainingsForDesignation(String search )
    {
        return this.query( () -> this.trainingDao.getTrainingForDesignation( search ) );
    }

    public List<Training> getTrainingsSortedByDesignation()
    {
        return this.query( () -> this.trainingDao.getTrainingSortedByDesignation() );
    }

    private List<Training> query( Callable<List<Training>> query )
    {
        try {
            return MyFitnessBuddyDatabase.executeWithReturn( query );
        }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public Training getLastTraining() {
        try {
            return MyFitnessBuddyDatabase.executeWithReturn( this.trainingDao::getLastEntry );
        }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // Well, is this a reasonable default return value?
        return new Training("", Category.none);
    }

    public void update(Training training) {
        training.setModified( System.currentTimeMillis() );
        training.setVersion( training.getVersion() + 1 );

        MyFitnessBuddyDatabase.execute( () -> trainingDao.update( training) );
    }

    public void insert(Training training) {
        training.setCreated( System.currentTimeMillis() );
        training.setModified( training.getCreated() );
        training.setVersion( 1 );

        MyFitnessBuddyDatabase.execute( () -> trainingDao.insert( training ) );
    }


    public long insertAndWait( Training training ) {
        training.setCreated( System.currentTimeMillis() );
        training.setModified( training.getCreated() );
        training.setVersion( 1 );

        try {
            return MyFitnessBuddyDatabase.executeWithReturn( () -> trainingDao.insert( training ) );
        }
        catch (ExecutionException | InterruptedException e)
        {
            e.printStackTrace();
        }

        return -1;
    }
}