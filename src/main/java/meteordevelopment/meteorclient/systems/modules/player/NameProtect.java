package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NameProtect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> nameProtect = sgGeneral.add(new BoolSetting.Builder()
        .name("name-protect")
        .description("Hides your name client-side.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("Name to be replaced with.")
        .defaultValue("seasnail") //Lets pay respect for his impact to project
        .visible(nameProtect::get)
        .build()
    );

    private final Setting<Boolean> skinProtect = sgGeneral.add(new BoolSetting.Builder()
        .name("skin-protect")
        .description("Make players become Steves.")
        .defaultValue(true)
        .build()
    );

    private String username = "";

    public NameProtect() {
        super(Categories.Player, "name-protect", "Hide player names and skins.");
    }

    @Override
    public void onActivate() {
        if (mc.getSession() != null) {
            username = mc.getSession().getUsername();
        }
    }

    public String replaceName(String string) {
        if (string != null && isActive() && nameProtect.get() && !username.isEmpty()) {
            return string.replace(username, name.get());
        }
        return string;
    }

    public String getName(String original) {
        if (isActive() && nameProtect.get() && !name.get().isEmpty()) {
            return name.get();
        }
        return original;
    }

    public boolean skinProtect() {
        return isActive() && skinProtect.get();
    }
}
