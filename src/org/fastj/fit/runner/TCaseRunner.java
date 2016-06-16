package org.fastj.fit.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.Parameter;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TSuite;
import org.fastj.fit.tool.TCSLoader;
import org.fastj.fit.tool.TRun;
import org.fastj.fit.tool.TSysInit;

public class TCaseRunner {
	
	private TProject setup = null;
	private TProject teardown = null;
	private List<TProject> tmodules = new ArrayList<>();
	
	public void run(String[] args) throws DataInvalidException
	{
		TProject tproj = new TProject();
		TSysInit.loadPlugins(tproj);
		
		ParameterTable argTable = new ParameterTable();
		List<String> filelist = new ArrayList<String>();
		
		for (String arg : args)
		{
			if (arg.startsWith("-P") || arg.startsWith("--"))
			{
				String pv[] = arg.substring(2).split("=", 2);
				if (pv.length == 2)
				{
					argTable.add(pv[0], pv[1]);
				}
				else
				{
					argTable.add(pv[0], "true");
				}
			}
			else if (arg.startsWith("-f"))
			{
				filelist.add(arg.substring(2));
			}
		}
		
		tproj.setDir(argTable.getPara("project", "."));
		List<File> runfs = null;
		if (filelist.isEmpty())
		{
			runfs = tproj.getRunFiles();
		}
		else
		{
			runfs = new ArrayList<>();
			for (String f : filelist)
			{
				File ff = tproj.getProjectFile(f);
				if (ff != null)
				{
					runfs.add(ff);
				}
			}
		}
		
		if (runfs.isEmpty())
		{
			LogUtil.error("No run file find.");
			return;
		}
		
		LogUtil.level = argTable.getInt("loglevel", LogUtil.INFO);
		
		for (File rf : runfs)
		{
			TProject nproj = new TProject();
			nproj.setPostProc(cloneT(tproj.getPostProc()));
			nproj.setPerfStat(cloneT(tproj.getPerfStat()));
			
			TCSLoader.loadV2(nproj, rf);
			for (Parameter p : argTable.gets())
			{
				nproj.getSysVars().add(p.getName(), p.getValue(), p.getDesc());
			}
			
			if ("setup.fitrun".equals(rf.getName())){
				setup = nproj;
			}
			else if ("teardown.fitrun".equals(rf.getName())){
				teardown = nproj;
			}
			else{
				tmodules.add(nproj);
			}
		}
		
		for (File aws : tproj.getAWscripts())
		{
			TCSLoader.loadAWScripts(aws);
		}
		
		if (setup != null) run(setup);
		
		if (!tmodules.isEmpty())
		{
			final CountDownLatch cdl = new CountDownLatch(tmodules.size());
			ExecutorService exec = Executors.newFixedThreadPool(tmodules.size());
			for (TProject tp : tmodules)
			{
				exec.execute(new Runnable() {
					public void run() {
						if (setup != null)
						{
							tp.getSysVars().setParent(setup.getSysVars());
						}
						try {
							TCaseRunner.this.run(tp);
						} catch (Throwable e) {
							LogUtil.error("Run inner project fail: e={}", e.getMessage());
						}
						cdl.countDown();
					}
				});
			}
			
			try {
				cdl.await();
			} catch (InterruptedException e) {
			}
		}
		
		if (teardown != null){
			if (setup != null)
			{
				teardown.getSysVars().setParent(setup.getSysVars());
			}
			run(teardown);
		}
		
		if (!RunMain.embedded){
			System.exit(0);
		}
	}
	
	private void run(TProject tproj)
	{
		tproj.start();
		int maxL = tproj.getMaxLevel();
		for (int level = -1; level <= maxL; level++)
		{
			List<TSuite> tsl = tproj.getSuites(level);
			TRun.run(tproj, level, tsl);
		}
		
		tproj.waitAll();
		
		//report
		if (tproj.getPostProc() != null)
		{
			tproj.getPostProc().end();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T cloneT(T intf) throws DataInvalidException
	{
		if (intf == null) return null;
		
		try {
			return (T) intf.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LogUtil.error("PostProc or PerfStat can not init: {}", intf.getClass());
			throw new DataInvalidException("Provide default constructor for " + intf.getClass());
		}
	}
	
}
