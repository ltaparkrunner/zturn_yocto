SUMMARY = "Prebuilt FSBL for MYIR Z-Turn"
LICENSE = "CLOSED"

PROVIDES += "virtual/fsbl fsbl"

SRC_URI = "file://fsbl.elf"

inherit deploy

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${WORKDIR}/fsbl.elf ${DEPLOYDIR}/fsbl-${MACHINE}.elf
    ln -sf fsbl-${MACHINE}.elf ${DEPLOYDIR}/fsbl.elf
}

addtask deploy after do_install

