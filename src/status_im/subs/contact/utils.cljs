(ns status-im.subs.contact.utils
  (:require
    [native-module.core :as native-module]
    [status-im.common.pixel-ratio :as pixel-ratio]
    [status-im.constants :as constants]
    [utils.address :as address]))

(defn replace-contact-image-uri
  [{:keys [contact port public-key font-file theme]}]
  (let [{:keys [images ens-name customization-color]} contact
        images
        (reduce (fn [acc image]
                  (let [image-name (:type image)
                        clock      (:clock image)
                        options    {:port           port
                                    :ratio          pixel-ratio/ratio
                                    :public-key     public-key
                                    :image-name     image-name
                                    ; We pass the clock so that we reload the
                                    ; image if the image is updated
                                    :clock          clock
                                    :theme          theme
                                    :override-ring? (when ens-name false)}]
                    (assoc-in acc
                     [(keyword image-name) :config]
                     {:type    :contact
                      :options options})))
                images
                (vals images))

        images (if (seq images)
                 images
                 {:thumbnail
                  {:config {:type    :initials
                            :options {:port                port
                                      :ratio               pixel-ratio/ratio
                                      :public-key          public-key
                                      :override-ring?      (when ens-name false)
                                      :uppercase-ratio     (:uppercase-ratio
                                                            constants/initials-avatar-font-conf)
                                      :customization-color customization-color
                                      :theme               theme
                                      :font-file           font-file}}}})]

    (assoc contact :images images)))


(defn build-contact-from-public-key
  [public-key]
  (when public-key
    (let [compressed-key (native-module/serialize-legacy-key public-key)]
      {:public-key     public-key
       :compressed-key compressed-key
       :primary-name   (address/get-shortened-compressed-key (or compressed-key public-key))})))
