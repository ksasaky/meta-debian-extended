# base recipe: meta/recipes-extended/iptables/iptables_1.6.2.bb
# base branch: warrior
# base commit: cd4b8a8553f9d551af27941910cf4d3405ecb7b0

SUMMARY = "Tools for managing kernel packet filtering capabilities"
DESCRIPTION = "iptables is the userspace command line program used to configure and control network packet \
filtering code in Linux."
HOMEPAGE = "http://www.netfilter.org/"
BUGTRACKER = "http://bugzilla.netfilter.org/"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263\
                    file://iptables/iptables.c;beginline=13;endline=25;md5=c5cffd09974558cf27d0f763df2a12dc"

inherit debian-package
require recipes-debian/sources/iptables.inc

FILESPATH_append = ":${COREBASE}/meta/recipes-extended/iptables/iptables:"
SRC_URI += " \
           file://run-ptest \
           file://0001-configure-Add-option-to-enable-disable-libnfnetlink.patch \
           file://0002-configure.ac-only-check-conntrack-when-libnfnetlink-enabled.patch \
"

inherit autotools pkgconfig ptest

EXTRA_OECONF = "--with-kernel=${STAGING_INCDIR}"

PACKAGECONFIG ?= "${@bb.utils.filter('DISTRO_FEATURES', 'ipv6', d)}"

PACKAGECONFIG[ipv6] = "--enable-ipv6,--disable-ipv6,"

# libnfnetlink recipe is in meta-networking layer
PACKAGECONFIG[libnfnetlink] = "--enable-libnfnetlink,--disable-libnfnetlink,libnfnetlink libnetfilter-conntrack"

# libnftnl recipe is in meta-networking layer(previously known as libnftables)
PACKAGECONFIG[libnftnl] = "--enable-nftables,--disable-nftables,libnftnl bison-native"

do_configure_prepend() {
	# Remove some libtool m4 files
	# Keep ax_check_linker_flags.m4 which belongs to autoconf-archive.
	rm -f libtool.m4 lt~obsolete.m4 ltoptions.m4 ltsugar.m4 ltversion.m4
}

PACKAGES += "${PN}-modules"
PACKAGES_DYNAMIC += "^${PN}-module-.*"

python populate_packages_prepend() {
    modules = do_split_packages(d, '${libdir}/xtables', r'lib(.*)\.so$', '${PN}-module-%s', '${PN} module %s', extra_depends='')
    if modules:
        metapkg = d.getVar('PN') + '-modules'
        d.appendVar('RDEPENDS_' + metapkg, ' ' + ' '.join(modules))
}

FILES_${PN} += "${datadir}/xtables"

ALLOW_EMPTY_${PN}-modules = "1"

RDEPENDS_${PN} = "${PN}-module-xt-standard"
RRECOMMENDS_${PN} = " \
    ${PN}-modules \
    kernel-module-x-tables \
    kernel-module-ip-tables \
    kernel-module-iptable-filter \
    kernel-module-iptable-nat \
    kernel-module-nf-defrag-ipv4 \
    kernel-module-nf-conntrack \
    kernel-module-nf-conntrack-ipv4 \
    kernel-module-nf-nat \
    kernel-module-ipt-masquerade \
"

do_install_ptest () {
    install -d ${D}${PTEST_PATH}/iptables/tests

    install -m755 ${B}/iptables/.libs/* ${D}${PTEST_PATH}/iptables
    cp -r ${B}/extensions ${D}${PTEST_PATH}
    cp -r ${S}/iptables/tests/shell ${D}${PTEST_PATH}/iptables/tests

    # handle multilib
    sed -i s:@libdir@:${libdir}:g ${D}${PTEST_PATH}/run-ptest
}

RDEPENDS_${PN}-ptest = "bash diffutils findutils util-linux"
RRECOMMENDS_${PN}-ptest = " \
    kernel-module-iptable-mangle \
    kernel-module-iptable-raw \
    kernel-module-iptable-security \
    kernel-module-ip6-tables \
    kernel-module-ip6table-raw \
    kernel-module-ip6table-mangle \
    kernel-module-ip6table-nat \
    kernel-module-ip6table-filter \
    kernel-module-ip6table-security \
    kernel-module-nfnetlink \
    kernel-module-nfnetlink-log \
    kernel-module-xt-nflog \
    kernel-module-xt-multiport \
    kernel-module-xt-log \
    kernel-module-xt-mac \
    kernel-module-xt-tcpmss \
    kernel-module-xt-limit \
    kernel-module-xt-ct \
    kernel-module-xt-helper \
    kernel-module-xt-checksum \
    kernel-module-xt-state \
    kernel-module-xt-mark \
    kernel-module-xt-comment \
    kernel-module-xt-tcpudp \
    kernel-module-xt-conntrack \
    kernel-module-nf-log-common \
    kernel-module-nf-log-ipv4 \
    kernel-module-nf-conntrack-pptp \
    kernel-module-nf-conntrack-netbios-ns \
    kernel-module-ipt-reject \
    kernel-module-ip6t-reject \
"
