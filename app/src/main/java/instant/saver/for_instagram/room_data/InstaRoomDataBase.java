package instant.saver.for_instagram.room_data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.jetbrains.annotations.NotNull;

import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;


@Database(entities = {Saved_Profile.class, Album_Data.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class InstaRoomDataBase extends RoomDatabase {

        public abstract SavedProfileDao savedProfileDao();

        public abstract AlbumDataDao albumDataDao();

        private static volatile InstaRoomDataBase INSTANCE;

        public static InstaRoomDataBase getDatabase(final Context context) {
            if (INSTANCE == null) {
                synchronized (InstaRoomDataBase.class) {
                    INSTANCE = Room.databaseBuilder(context,
                            InstaRoomDataBase.class, "InstaRoom_Database")
//                            .addCallback(sRoomDatabaseCallBack)
//                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
            return INSTANCE;
        }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {
//            database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, "
//                    + "`name` TEXT, PRIMARY KEY(`id`))");
        }
    };

//        /*private static final RoomDatabase.Callback sRoomDatabaseCallBack = new RoomDatabase.Callback() {
//            @Override
//            public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                super.onCreate(db);
//                dataBaseWriteExecutor.execute(() -> {
//                    ContactDao contactDao = INSTANCE.contactDao();
//                    Contact contact = new Contact("Roshan", "Student");
//
//                    contactDao.insert(contact);
//                    contactDao.insert(new Contact("RAhul", "Cricketer"));
//                });
//            }
//        };*/

}
