package com.ineedwhite.diancan.biz.impl;

import com.ineedwhite.diancan.biz.DianCanConfig;
import com.ineedwhite.diancan.dao.dao.BoardDao;
import com.ineedwhite.diancan.dao.domain.BoardDo;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ruanxin
 * @create 2018-03-08
 * @desc
 */
@Service
public class DianCanConfigImpl implements DianCanConfig{
    private Logger logger = Logger.getLogger(DianCanConfigImpl.class);

    @Resource
    private BoardDao boardDao;

    private static List<BoardDo> boardDoList = new ArrayList<BoardDo>();

    /**
     * 桌位缓存信息
     */
    private static Map<Integer, BoardDo> boardDoCache = new ConcurrentHashMap<Integer, BoardDo>();

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    @PostConstruct
    private void init() {
        logger.info("load boardConfig information start!");
        refreshConfig();

        //定时刷新页面 5分钟一次
        executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
               refreshConfig();
            }
        },5, 5, TimeUnit.MINUTES);
    }

    public boolean refreshConfig() {
        try {
            logger.info("refresh config information start!");
            boardDoList = boardDao.findAllBoardInfo();
            Map<Integer, BoardDo> newBoardMapCache = new ConcurrentHashMap<Integer, BoardDo>();
            for (BoardDo boardDo : boardDoList) {
                newBoardMapCache.put(boardDo.getBoard_id(), boardDo);
            }
            boardDoCache = newBoardMapCache;
        } catch (Exception ex) {
            logger.error("refreshGateConfig exception:" + ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public Map<Integer, BoardDo> getAllBoard() {
        return boardDoCache;
    }
}