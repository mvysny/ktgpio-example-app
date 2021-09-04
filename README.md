# ktgpio Example App

A Kotlin/Native example app which uses the [ktgpio](https://github.com/ktgpio/ktgpio) to control
[Raspberry PI GPIO](https://pinout.xyz/#) pins.

This example app only activates the GPIO17 (pin #11) for a second, then turns it off.
For a more complex example please take look at the sources of the [ktgpio-samples](https://github.com/ktgpio/ktgpio-samples/)
example app.

## Prerequisites

Kotlin/Native at the moment doesn't support running on arm64: you'll get
"unknown host target: linux aarch64" error if you try; see the
[getting 'unknown host target: linux aarch64'](https://discuss.kotlinlang.org/t/kotlin-native-getting-unknown-host-target-linux-aarch64-on-raspberry-pi-3b-ubuntu-21-04-aarch64/22874)
forum and also [KT-42445](https://youtrack.jetbrains.com/issue/KT-42445) for more details.

Therefore, you can not build this project on the Raspberry PI itself - you'll need to build this project
on an x86-64 machine (Intel/AMD) via a process called cross-compiling (that is, compiling a binary which runs on a CPU with different architecture).

According to [Kotlin/Multiplatform Supported Platforms](https://kotlinlang.org/docs/mpp-supported-platforms.html)
building for linuxArm64 may not necessarily require running the build on a Linux machine, therefore you
may have luck building this project on Windows and Mac as well. However, if there are any issues,
make sure to build this project on a Linux machine (I'm using Ubuntu 21.04 x86-64 and the project builds just fine).

Obviously you'll need a Raspberry PI. By default this project builds an arm64 binary which only works
on 64-bit Linux (I'm running Ubuntu 21.04 arm64 on my RPI 3B with 1G of RAM but the binary should work on any arm64 Linux, say on
RPI 4). If you'd like to build this project for an arm32 Linux, see below for a list of tips.

Make sure to install the gpio and i2c packages on your Raspberry PI before you try to run the binary,
for example by running
`sudo apt install libi2c0 libgpiod gpiod`.

## Building

Simply run `./gradlew`. The project should build fine and the binary .kexe should be present
in the `build/bin/native/releaseExecutable/` folder.

As stated above, I'm building this project on my Ubuntu 21.04 x86-64 machine and it works properly,
however the build should create an arm64 binary even when running on MacOsX or Windows.

## Running

The binary will obviously only run on an arm64 Linux. It needs to have the `libi2c0` and `libgpiod` libraries
installed (those are needed by the ktgpio library). Simply copy the binary from your desktop machine to your
Raspberry PI, for example via ssh by using the scp program.

You'll probably need to run this app with sudo, in order to be able to access
`/dev/mem` and `/dev/gpiomem`. See
https://raspberrypi.stackexchange.com/questions/40105/access-gpio-pins-without-root-no-access-to-dev-mem-try-running-as-root
for more details. Simply run the binary with `sudo` and you should be fine.

## Compiling a 32bit binary

This project by default builds a 64bit binary for arm64. If you are running a 32bit Linux
on your RPI, you must reconfigure this project to produce a 32bit binary:

1. Edit `build.gradle.kts` and replace `linuxArm64("native")` with `linuxArm32Hfp("native")`.
2. Replace both of the `libs/*.so` library files by their 32bit counterparts downloaded from
  your arm32 Linux running on your RPI. See [libs/README.txt](libs/README.txt) for more details
  on where to obtain the files.
