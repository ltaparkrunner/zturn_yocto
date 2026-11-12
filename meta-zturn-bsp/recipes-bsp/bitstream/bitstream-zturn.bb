SUMMARY = "Prebuilt FPGA bitstream for Z-Turn HDMI"
LICENSE = "CLOSED"

PROVIDES += "virtual/bitstream"

SRC_URI = "file://HDMI6_wrapper.bit"

inherit deploy

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${WORKDIR}/HDMI6_wrapper.bit ${DEPLOYDIR}/bitstream-${MACHINE}.bit
    ln -sf bitstream-${MACHINE}.bit ${DEPLOYDIR}/download.bit
}

addtask deploy after do_install

