package ru.clonebox.filesystem;


import ru.clonebox.common.Util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watcher implements Runnable {
    private final WatchService watcher;
    private final boolean recursive;
    private boolean trace = false;
    private int count;
    private Thread watcherThread;
    private Reaction reaction;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }


    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        count++;
        if (trace)
            System.out.format("register: %s\n", dir);
    }


    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public Watcher(Path dir, boolean recursive, Reaction reaction) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.recursive = recursive;
        this.reaction = reaction;
        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            Util.log("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
        watcherThread = Thread.currentThread();
    }


    @Override
    public void run() {
        while (!watcherThread.isInterrupted()) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

           /* for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = ((Path)key.watchable()).resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }*/
            key.pollEvents().clear();

            /**past own code*/
            reaction.react();

//            Platform.runLater(()->windowClient.updateLocalPanel()); //сделать абстрактным

            // reset key
            boolean valid = key.reset();
            if (!valid) {
                // directory no longer accessible
                count--;
                if (count == 0)
                    break;
            }
        }
    }

}
