(ns status-im.subs.contact.utils
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [status-im.common.pixel-ratio :as pixel-ratio]
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
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

(defn- build-contact-from-public-key*
  [public-key]
  (when public-key
    (let [compressed-key (native-module/serialize-legacy-key public-key)]
      {:public-key     public-key
       :compressed-key compressed-key
       :primary-name   (address/get-shortened-compressed-key (or compressed-key public-key))})))

(def build-contact-from-public-key
  "The result of this function is stable because it relies exclusively on the
  public key, but it's not cheap to be performed hundreds of times in a row,
  such as when displaying a long list of channel members."
  (memoize build-contact-from-public-key*))

(defn contact-two-names
  [{:keys [primary-name] :as contact}
   {:keys [public-key preferred-name display-name name]}]
  [(if (= public-key (:public-key contact))
     (cond
       (not (string/blank? preferred-name)) preferred-name
       (not (string/blank? display-name))   display-name
       (not (string/blank? primary-name))   primary-name
       (not (string/blank? name))           name
       :else                                public-key)
     (profile.utils/displayed-name contact))
   (:secondary-name contact)])
