package dev.drawethree.xprison.ascensions;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.ascensions.api.XPrisonAscensionsAPI;
import dev.drawethree.xprison.ascensions.api.XPrisonAscensionsAPIImpl;
import dev.drawethree.xprison.ascensions.commands.MaxAscensionCommand;
import dev.drawethree.xprison.ascensions.commands.AscensionAdminCommand;
import dev.drawethree.xprison.ascensions.commands.AscensionCommand;
import dev.drawethree.xprison.ascensions.commands.AscensionTopCommand;
import dev.drawethree.xprison.ascensions.config.AscensionConfig;
import dev.drawethree.xprison.ascensions.listener.AscensionListener;
import dev.drawethree.xprison.ascensions.manager.AscensionManager;
import dev.drawethree.xprison.ascensions.repo.AscensionRepository;
import dev.drawethree.xprison.ascensions.repo.impl.AscensionRepositoryImpl;
import dev.drawethree.xprison.ascensions.service.AscensionService;
import dev.drawethree.xprison.ascensions.service.impl.AscensionServiceImpl;
import dev.drawethree.xprison.ascensions.task.SavePlayerDataTask;
import lombok.Getter;

@Getter
public final class XPrisonAscensions implements XPrisonModule {

	public static final String MODULE_NAME = "Ascensions";

	@Getter
	private AscensionConfig ascensionConfig;

	private AscensionManager ascensionManager;

	@Getter
	private XPrisonAscensionsAPI api;

	private SavePlayerDataTask savePlayerDataTask;

	@Getter
	private final XPrison core;

	@Getter
	private AscensionRepository ascensionRepository;

	@Getter
	private AscensionService ascensionService;

	private boolean enabled;

	public XPrisonAscensions(XPrison core) {
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.ascensionConfig.reload();
	}

	@Override
	public void enable() {
		this.enabled = true;

		this.ascensionConfig = new AscensionConfig(this);
		this.ascensionConfig.load();

		this.ascensionRepository = new AscensionRepositoryImpl(this.core.getPluginDatabase());
		this.ascensionRepository.createTables();

		this.ascensionService = new AscensionServiceImpl(this.ascensionRepository);

		this.ascensionManager = new AscensionManager(this);
		this.ascensionManager.enable();

		this.api = new XPrisonAscensionsAPIImpl(this);

		this.savePlayerDataTask = new SavePlayerDataTask(this);
		this.savePlayerDataTask.start();

		this.registerCommands();
		this.registerListeners();
	}


	@Override
	public void disable() {
		this.savePlayerDataTask.stop();
		this.ascensionManager.disable();
		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

	@Override
	public void resetPlayerData() {
		this.ascensionRepository.clearTableData();
	}

	private void registerCommands() {
		new AscensionCommand(this).register();
		new MaxAscensionCommand(this).register();
		new AscensionTopCommand(this).register();
		new AscensionAdminCommand(this).register();
	}

	private void registerListeners() {
		new AscensionListener(this).register();
	}
}
