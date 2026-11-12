# Yocto BSP для платы MYIR Z-Turn (Zynq-7000)

Проект сборки Linux-образа на базе **Yocto Project (`scarthgap`)** для платы **MYIR Z-Turn** с использованием кастомного слоя поддержки платы **`meta-zturn-bsp`** (включающего внешний FSBL, прошивку ПЛИС `HDMI6_wrapper.bit` и дерево устройств `zynq-zturn.dts`).

---

## 1. Установка зависимостей хост-системы (Ubuntu / Linux Mint)

```bash
sudo apt-get update
sudo apt-get install -y gawk wget git diffstat unzip texinfo gcc build-essential \
  chrpath socat cpio python3 python3-pip python3-pexpect xz-utils debianutils \
  iputils-ping python3-git python3-jinja2 libegl1-mesa libsdl1.2-dev pylint \
  xterm zstd liblz4-tool tree
```

---

## 2. Клонирование репозитория и базовых слоёв Yocto

Если вы развёртываете проект с нуля, перейдите в корень репозитория `zturn_yocto` и скачайте внешние слои ветки `scarthgap`:

```bash
# Ядро Yocto (Poky)
git clone -b scarthgap https://git.yoctoproject.org/poky

# Аппаратные и вспомогательные слои
git clone -b scarthgap https://git.yoctoproject.org/meta-xilinx
git clone -b scarthgap https://github.com/openembedded/meta-openembedded.git
git clone -b scarthgap https://git.yoctoproject.org/meta-arm
```

---

## 3. Структура кастомного слоя `meta-zturn-bsp`

Все аппаратные файлы и рецепты платы хранятся в слое `meta-zturn-bsp/`:

```text
meta-zturn-bsp/
├── conf/
│   ├── layer.conf
│   └── machine/
│       └── zturn-zynq7.conf
└── recipes-bsp/
    ├── bitstream/
    │   ├── bitstream-zturn.bb
    │   └── files/
    │       └── HDMI6_wrapper.bit
    ├── device-tree/
    │   ├── device-tree.bbappend
    │   └── files/
    │       ├── skeleton.dtsi
    │       ├── zynq-7000.dtsi
    │       └── zynq-zturn.dts
    └── fsbl/
        ├── files/
        │   └── fsbl.elf
        └── fsbl.bb
```

---

## 4. Инициализация окружения и подключение слоёв

Выполните из корня репозитория `zturn_yocto`:

```bash
source poky/oe-init-build-env zturn_build
```

Если файл `zturn_build/conf/bblayers.conf` ещё не настроен, подключите все необходимые слои (включая `meta-zturn-bsp` с приоритетом `6`):

```bash
bitbake-layers add-layer ../meta-openembedded/meta-oe
bitbake-layers add-layer ../meta-arm/meta-arm-toolchain
bitbake-layers add-layer ../meta-arm/meta-arm
bitbake-layers add-layer ../meta-xilinx/meta-xilinx-core
bitbake-layers add-layer ../meta-xilinx/meta-xilinx-bsp
bitbake-layers add-layer ../meta-xilinx/meta-xilinx-vendor
bitbake-layers add-layer ../meta-zturn-bsp
```

Проверьте список подключённых слоёв:

```bash
bitbake-layers show-layers
```

---

## 5. Настройка `zturn_build/conf/local.conf`

Убедитесь, что в файле `zturn_build/conf/local.conf` выбрана целевая машина `zturn-zynq7` и форматы образов:

```bitbake
MACHINE = "zturn-zynq7"
IMAGE_FSTYPES = "wic wic.gz cpio.gz"
```

---

## 6. Сборка образов

Для предварительной проверки отдельно дерева устройств и загрузочного `boot.bin` (FSBL + Bitstream + U-Boot):

```bash
bitbake device-tree xilinx-bootbin
```

Для сборки полного минимального образа системы:

```bash
bitbake core-image-minimal
```

Готовые артефакты сохраняются в директории `tmp/deploy/images/zturn-zynq7/`:
* `core-image-minimal-zturn-zynq7.rootfs.wic` / `.wic.gz` — готовый образ для SD-карты.
* `boot.bin` — загрузочный образ (FSBL + `HDMI6_wrapper.bit` + U-Boot).
* `zynq-zturn.dtb` — скомпилированное дерево устройств.

---

## 7. Запись образа на SD-карту и запуск

1. Определите имя устройства вашей SD-карты командой `lsblk` (например, `/dev/sdb` — **будьте внимательны, чтобы не перезаписать системный диск**).
2. Запишите `.wic`-образ на карту:

```bash
sudo dd if=tmp/deploy/images/zturn-zynq7/core-image-minimal-zturn-zynq7.rootfs.wic of=/dev/sdX bs=4M status=progress conv=fsync
```

*(Опционально) Запуск базовой эмуляции в QEMU:*

```bash
runqemu zturn-zynq7 core-image-minimal
```

