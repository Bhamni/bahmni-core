package org.bahmni.module.bahmnicore.dao;

import java.io.File;

public interface ApplicationDataDirectory {
    File getFile(String relativePath);

    File getFileFromConfig(String relativePath);
}
