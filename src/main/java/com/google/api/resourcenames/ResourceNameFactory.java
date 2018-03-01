package com.google.api.resourcenames;

public interface ResourceNameFactory {

  /* Create a new ResourceName from a formatted String representing a ResourceName. */
  ResourceName parseFrom(String formattedString);
}
