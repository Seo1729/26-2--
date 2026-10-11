import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import settings.GameSettings;

/** 실제 오디오 장치 없이 기존 Clip 재생 계약과 음량 변경을 검증한다. */
public class SoundVolumeCheck {
    static class Gain extends FloatControl {
        Gain() { super(Type.MASTER_GAIN, -80, 6, 0.1f, 0, 0, "dB"); }
    }
    static class Device {
        final Gain gain = new Gain();
        final boolean nativeGain;
        boolean running, closed;
        int position, starts, loops;
        byte[] data;
        final Clip clip;
        Device(boolean nativeGain) {
            this.nativeGain = nativeGain;
            clip = (Clip) Proxy.newProxyInstance(Clip.class.getClassLoader(), new Class<?>[] {Clip.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                    case "open": data = ((byte[]) args[1]).clone(); return null;
                    case "isControlSupported": return this.nativeGain;
                    case "getControl": return gain;
                    case "start": running = true; starts++; return null;
                    case "loop": running = true; loops++; return null;
                    case "stop": running = false; return null;
                    case "close": closed = true; return null;
                    case "isRunning": return running;
                    case "getFramePosition": return position;
                    case "setFramePosition": position = (Integer) args[0]; return null;
                    case "getFrameLength": return data.length / 2;
                    case "hashCode": return System.identityHashCode(proxy);
                    case "equals": return proxy == args[0];
                    default: throw new AssertionError("Unexpected Clip call: " + method.getName());
                    }
                });
        }
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("reload")) {
            check(Settings.getBgmVolume() == 25 && Settings.getSfxVolume() == 35, "Process restart settings");
            check(!Settings.isMuted(), "Mute persistence");
            System.out.println("Restart persistence checks passed.");
            return;
        }
        List<Device> devices = new ArrayList<>();
        SoundManager.clipFactory = () -> {
            Device device = new Device(true); devices.add(device); return device.clip;
        };
        Settings.setBgmVolume(100);
        Settings.setSfxVolume(100);
        SoundManager.startBgm(Bgm.MENU);
        Device bgm = devices.get(0);
        bgm.position = 123;
        SoundManager.startBgm(Bgm.MENU);
        check(devices.size() == 1 && bgm.loops == 1, "Same track must continue");
        SoundManager.play(Sound.GAME_OVER);
        Device sfx = devices.get(1);
        Settings.setBgmVolume(50);
        Settings.setSfxVolume(25);
        check(Math.abs(bgm.gain.getValue() + 6.0206f) < 0.01f && bgm.position == 123, "Live BGM gain");
        check(Math.abs(sfx.gain.getValue() + 12.0412f) < 0.01f && sfx.running, "Live SFX gain");
        Settings.setBgmVolume(0);
        check(!bgm.running && sfx.running, "Independent BGM silence");
        Settings.setSfxVolume(0);
        check(!sfx.running, "Active SFX silence");
        int starts = sfx.starts;
        SoundManager.play(Sound.GAME_OVER);
        check(sfx.starts == starts, "Muted SFX must not play");
        Settings.setBgmVolume(60);
        Settings.setSfxVolume(60);
        check(bgm.running && bgm.position == 123 && !sfx.running, "Resume BGM without replaying SFX");
        SoundManager.play(Sound.GAME_OVER);
        check(sfx.starts == starts + 1 && sfx.position == 0, "Existing repeat effect behavior");
        Settings.setMuted(true);
        check(!bgm.running && !sfx.running, "Global mute");
        Settings.setMuted(false);
        SoundManager.stopBgm();

        devices.clear();
        SoundManager.clipFactory = () -> {
            Device device = new Device(false); devices.add(device); return device.clip;
        };
        SoundManager.startBgm(Bgm.GAME);
        Device software = devices.get(devices.size() - 1);
        software.position = 456;
        Settings.setBgmVolume(30);
        Device replacement = devices.get(devices.size() - 1);
        check(software.closed && replacement.running && replacement.position == 456, "PCM fallback position/loop");
        byte[] original = SoundManager.makeBgm(Bgm.GAME);
        check(java.util.Arrays.equals(replacement.data, SoundManager.scalePcm(original, 0.3f)), "PCM absolute scaling");
        SoundManager.play(Sound.WIN);
        Device effect = devices.get(devices.size() - 1);
        effect.position = 200;
        Settings.setSfxVolume(40);
        Device newEffect = devices.get(devices.size() - 1);
        check(effect.closed && newEffect.running && newEffect.position == 200 && newEffect.loops == 0, "PCM active SFX");
        int count = devices.size();
        SoundManager.play(Sound.WIN);
        check(devices.size() == count && newEffect.position == 0, "Fallback cache reuse");
        Settings.setBgmVolume(0);
        Settings.setSfxVolume(0);
        check(!replacement.running && !newEffect.running, "Fallback silence");
        Settings.setBgmVolume(25);
        Settings.setSfxVolume(35);
        GameSettings saved = new GameSettings();
        check(saved.getBgmVolume() == 25 && saved.getSfxVolume() == 35, "File persistence");
        check(Settings.getSaveError() == null, "Successful save");
        check(java.util.Arrays.equals(SoundManager.scalePcm(new byte[] {-1, 127, 0, -128}, 0), new byte[4]), "PCM zero");
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            VolumeSettingsPanel panel = new VolumeSettingsPanel();
            for (java.awt.Component component : panel.getComponents()) {
                if (component instanceof javax.swing.JSlider) {
                    javax.swing.JSlider slider = (javax.swing.JSlider) component;
                    check(slider.getMinimum() == 0 && slider.getMaximum() == 100, "Slider range");
                }
            }
        });
        SoundManager.stopBgm();
        SoundManager.clipFactory = () -> { throw new javax.sound.sampled.LineUnavailableException(); };
        SoundManager.play(Sound.SHOOT);
        SoundManager.startBgm(Bgm.MENU);
        java.nio.file.Path file = saved.getFile();
        java.nio.file.Files.delete(file);
        java.nio.file.Files.createDirectory(file);
        Settings.setBgmVolume(20);
        check(Settings.getBgmVolume() == 20 && Settings.getSaveError() != null, "Save failure must preserve live value");
        java.nio.file.Files.delete(file);
        Settings.setBgmVolume(25);
        System.out.println("Sound volume checks passed.");
    }
}
