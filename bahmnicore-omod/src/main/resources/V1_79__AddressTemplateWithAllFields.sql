update global_property
set property_value = '<org.openmrs.layout.web.address.AddressTemplate>
                        <nameMappings class="properties">
                          <property name="address1" value="Location.address1"/>
                          <property name="address2" value="Location.address2"/>
                          <property name="address3" value="Location.address3"/>
                          <property name="address4" value="Location.address4"/>
                          <property name="address5" value="Location.address5"/>
                          <property name="address6" value="Location.address6"/>
                          <property name="postalCode" value="Location.postalCode"/>
                          <property name="longitude" value="Location.longitude"/>
                          <property name="startDate" value="PersonAddress.startDate"/>
                          <property name="country" value="Location.country"/>
                          <property name="countyDistrict" value="Location.district"/>
                          <property name="endDate" value="personAddress.endDate"/>
                          <property name="stateProvince" value="Location.stateProvince"/>
                          <property name="latitude" value="Location.latitude"/>
                          <property name="cityVillage" value="Location.cityVillage"/>
                        </nameMappings>
                        <sizeMappings class="properties">
                          <property name="postalCode" value="10"/>
                          <property name="longitude" value="10"/>
                          <property name="address2" value="40"/>
                          <property name="address1" value="40"/>
                          <property name="startDate" value="10"/>
                          <property name="country" value="10"/>
                          <property name="endDate" value="10"/>
                          <property name="stateProvince" value="10"/>
                          <property name="latitude" value="10"/>
                          <property name="cityVillage" value="10"/>
                        </sizeMappings>
                        <lineByLineFormat>
                          <string>address1</string>
                          <string>address2</string>
                          <string>cityVillage stateProvince country postalCode</string>
                          <string>latitude longitude</string>
                          <string>startDate endDate</string>
                        </lineByLineFormat>
                      </org.openmrs.layout.web.address.AddressTemplate>'
where property = 'layout.address.format';
