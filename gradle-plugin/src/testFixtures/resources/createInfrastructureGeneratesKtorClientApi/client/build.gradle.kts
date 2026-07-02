jvmApplication {
    ciKraft {
        dependencies {
            infrastructure(projects.infra)
        }

        generateKtorResources {}
    }
}
