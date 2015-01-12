package org.instedd.sync4j.app;

import java.awt.MenuItem;
import java.nio.file.Paths;

import org.instedd.sync4j.RsyncCommandBuilder;
import org.instedd.sync4j.RsyncSynchronizer;
import org.instedd.sync4j.Settings;
import org.instedd.sync4j.tray.SystemTrays;
import org.instedd.sync4j.watcher.PathWatcher;
import org.instedd.sync4j.watcher.RsyncWatchListener;
import org.instedd.sync4j.watcher.RsyncWatchListener.SyncMode;

public class RSyncApplication {

  private final Settings settings;
  private final String tooltip;
  private final String imageFilename;
  private final SyncMode syncMode;

  private transient Thread thread;

  // TODO extract parameter object
  public RSyncApplication(Settings settings, String tooltip, String imageFilename, SyncMode syncMode) {
    this.settings = settings;
    this.tooltip = tooltip;
    this.imageFilename = imageFilename;
    this.syncMode = syncMode;
  }

  public void start() {
    RsyncSynchronizer synchronizer = newSynchronizer();
    // TODO log sync mode
    PathWatcher watcher = new PathWatcher(Paths.get(settings.localOutboxDir), new RsyncWatchListener(synchronizer, syncMode));
    thread = new Thread(watcher::watch, "watcher-thread");

    SystemTrays.open(tooltip, imageFilename, menu -> {
      MenuItem menuItem = new MenuItem("Stop Sync");
      menuItem.addActionListener(e -> thread.interrupt());
      menu.add(menuItem);
    });
    synchronizer.setUp();
    thread.start();
  }

  public boolean isRunning() {
    return thread != null && thread.isAlive();
  }

  public void stop() throws InterruptedException {
    if (thread != null) {
      thread.interrupt();
      thread.join();
    }
  }

  protected RsyncSynchronizer newSynchronizer() {
    RsyncCommandBuilder commandBuilder = new RsyncCommandBuilder(settings);
    RsyncSynchronizer synchronizer = new RsyncSynchronizer(commandBuilder);
    return synchronizer;
  }

}