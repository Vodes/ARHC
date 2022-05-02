# ARHC
A quick tool to batch rename and/or creating hardlinks for Anime
(this was mostly made to test that theme, I dont actually need or use it)

As of 0.1.0 with way better CLI functionality compared to the UI side

## Features
- Rename and/or Hardlink with naming templates
- Change mkv titles with naming templates
- Tag Fixing for various issues in Anime releases[^1]
  - JPN full subs -> ENG (& default)
  - Signs/Songs Tracks properly tagging as forced
- Automated audio conversion for lossless audio[^1][^2]
  - Depending on channel number with appropriate [Opus](https://opus-codec.org) bitrates
  - Giving less bitrate to commentary tracks

Just give CLI a spin with `java -jar AHRC-0.1.0.jar`

<details>
  <summary>Image(s)</summary>
  
  ![Preview](https://vodes.pw/i/Alex/TalsnVj6Vn5LMND.png)
</details>


## Dependencies
- FormDev [FlatLaf](https://mvnrepository.com/artifact/com.formdev/flatlaf) & [IntelliJ Theme Pack](https://mvnrepository.com/artifact/com.formdev/flatlaf-intellij-themes)
- [anitomyJ](https://mvnrepository.com/artifact/com.dgtlrepublic/anitomyJ)
- Apache [Commons-IO](https://mvnrepository.com/artifact/commons-io/commons-io)

[^1]: Requires **mkvinfo** and **mkvpropedit** in path (both included in [MKVToolNix](https://mkvtoolnix.download/))
[^2]: Requires [**ffmpeg**](https://ffmpeg.org/download.html) in path
