package mw.mapreduce;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.io.File;
import java.io.IOException;

import mw.mapreduce.reader.MWKeyValueReader;
import mw.mapreduce.reader.MWMergingReader;
import mw.mapreduce.reader.MWReduceReader;

import mw.mapreduce.core.MWJob;
import mw.mapreduce.core.MWContext.MWMapContext;
import mw.mapreduce.core.MWContext.MWReduceContext;
import mw.mapreduce.jobs.friendextract.MWFriendExtractJob;
import mw.mapreduce.jobs.friendsort.MWFriendSortJob;
import mw.mapreduce.jobs.friendcount.MWFriendCountJob;

class MWMapReduce {
    public static void main(String[] args) {
        String app = args[0];
        String filepath = args[1];
        String tmpprefix = args[2];
        String outprefix = args[3];
        int numMapper = args.length < 5 ? 3 : Integer.parseInt(args[4]);
        int numReducer = args.length < 6 ? 2 : Integer.parseInt(args[5]);
        final String dirForReducer = "./tmpfiles/";

        long total_bytes = 0;
        try {
            File file = new File(filepath);
            total_bytes = file.length();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MWJob job;
        switch (app) {
            case ("friendsort"):
                job = new MWFriendSortJob();
                break;
            case ("friendextract"):
                job = new MWFriendExtractJob();
                break;
            case ("friendcount"):
                job = new MWFriendCountJob();
                break;
            default:
                job = new MWJob();
        }

        ExecutorService esMapper = Executors.newFixedThreadPool(16);
        //ExecutorService esMapper = Executors.newSingleThreadExecutor();
        ExecutorService esReducer = Executors.newFixedThreadPool(16);
        //ExecutorService esReducer = Executors.newSingleThreadExecutor();

        long start = 0;
        long length = (int) Math.ceil((double) total_bytes / numMapper);

        for (int i = 0; i < numMapper; i++) {
            if (i == numMapper - 1) {
                length = total_bytes - length * i;
            }

            System.out.println("total bytes: " + total_bytes + " start: " + start + " length: " + length);

            try {
                MWMapContext ctx = new MWMapContext(
                    job.createInputReader(filepath, start, length),
                    "tmpfiles/",
                    tmpprefix + "-" + i,
                    numReducer,
                    job.getComparator());
                esMapper.execute(job.createMapper(ctx));                // Übergebenes Objekt muss run()-Methode aufweisen
            } catch (Exception e) {
                e.printStackTrace();
            }
            start += length;
        }
        esMapper.shutdown();
        try {
            esMapper.awaitTermination(600, TimeUnit.SECONDS);    // Wartet „10 * numMapper“ Sekunden, bevor es weitergeht; jeder Mapper hat sozusagen „10 * numMapper“ Sekunden Zeit, seine Aufgabe zu erledigen
                                                                            // Zeitlimit relativ hoch, 10s reichen nicht, deswegen lieber auf Nummer sicher
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        /* Dateinamen im Output-Ordner der Mapper sammeln (gegenwärtig „./tmpfiles/“) */
        final LinkedList<LinkedList<String>> filesOfMappers = new LinkedList<LinkedList<String>>();
        for(int i = 0; i < numReducer; i++){
            filesOfMappers.add(new LinkedList<String>());   // füge für jeden Reducer eine Liste hinzu, die die Pfade der Mapper-Dateien speichert, die der Reducer verarbeiten muss
        }
        String fileName;
        final File folder = new File(dirForReducer);
        for(File file : folder.listFiles()){
            fileName = file.getName();
            final int REDUCER_ID = Integer.parseInt(fileName.substring(fileName.length() - 1));
            final LinkedList<String> tmpFilesOfMapper = filesOfMappers.get(REDUCER_ID);
            tmpFilesOfMapper.add(dirForReducer + fileName);
        }

        for(int i = 0; i < numReducer; i++) {
            System.out.println("MAPREDUCE: " + filesOfMappers.toString());

            try{
                MWReduceContext ctx = new MWReduceContext(
                    new MWMergingReader(filesOfMappers.get(i), job.getComparator()),
                    Integer.toString(i),
                    "outfiles/",
                    outprefix);
                esReducer.execute(job.createReducer(ctx));
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        esReducer.shutdown();
        try {
            esReducer.awaitTermination(600 , TimeUnit.SECONDS);  // Wartet 10 Sekunden, bevor es weitergeht; jeder Reducer hat sozusagen 10s Zeit, seine Aufgabe zu erledigen
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}