package com.springwater.easybot.velocity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 更新检查器
 */
public class UpdateChecker {
    
    private ScheduledExecutorService scheduler;
    private boolean running = false;
    
    /**
     * 启动更新检查器
     */
    public void start() {
        if (running) {
            return;
        }
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
        running = true;
        
        // 每小时检查一次更新
        scheduler.scheduleAtFixedRate(this::checkForUpdates, 1, 60, TimeUnit.MINUTES);
        
        EasyBotVelocity.getInstance().getLogger().info("更新检查器已启动");
    }
    
    /**
     * 停止更新检查器
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        running = false;
        EasyBotVelocity.getInstance().getLogger().info("更新检查器已停止");
    }
    
    /**
     * 检查更新
     */
    private void checkForUpdates() {
        try {
            EasyBotVelocity plugin = EasyBotVelocity.getInstance();
            if (plugin == null || plugin.getBridgeClient() == null) {
                return;
            }
            
            // 通过Bridge客户端检查更新
            if (plugin.getBridgeClient().isReady()) {
                // 这里可以调用Bridge客户端的getNewVersion方法
                // 由于是异步操作，这里只是示例
                plugin.getLogger().debug("正在检查更新...");
            }
        } catch (Exception e) {
            EasyBotVelocity.getInstance().getLogger().warn("检查更新时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
}