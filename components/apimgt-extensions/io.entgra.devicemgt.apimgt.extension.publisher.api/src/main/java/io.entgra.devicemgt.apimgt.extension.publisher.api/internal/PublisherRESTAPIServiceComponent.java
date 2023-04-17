package io.entgra.devicemgt.apimgt.extension.publisher.api.internal;

import io.entgra.devicemgt.apimgt.extension.publisher.api.APIApplicationServices;
import io.entgra.devicemgt.apimgt.extension.publisher.api.APIApplicationServicesImpl;
import io.entgra.devicemgt.apimgt.extension.publisher.api.PublisherRESTAPIServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="io.entgra.devicemgt.apimgt.extension.publisher.api.internal.PublisherRESTAPIServiceComponent"
 * immediate="true"
 */
public class PublisherRESTAPIServiceComponent {

    private static Log log = LogFactory.getLog(PublisherRESTAPIServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing publisher API extension bundle");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

//            PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServices();
//            bundleContext.registerService(PublisherRESTAPIServices.class.getName(), publisherRESTAPIServices, null);
//            PublisherRESTAPIDataHolder.getInstance().setPublisherRESTAPIServices(publisherRESTAPIServices);

            APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
            bundleContext.registerService(APIApplicationServices.class.getName(), apiApplicationServices, null);
            PublisherRESTAPIDataHolder.getInstance().setApiApplicationServices(apiApplicationServices);

            if (log.isDebugEnabled()) {
                log.debug("API Application bundle has been successfully initialized");
            }
        } catch (Exception e) {
            log.error("Error occurred while initializing API Application bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }
}
