licenses(["notice"])

package(default_visibility = ["//visibility:public"])

genrule(
    name = "nomad-scheduler",
    srcs = ["nomad"],
    outs = ["heron-nomad"],
    cmd = "mv $< $@",
)
    
#filegroup(
#   name = "nomad",
#    srcs = select({
#        "//tools/platform:darwin": ["@nomad_mac//file"],
#        "//conditions:default": ["@nomad_linux//file"],
#    }),
#)
