(ns quo.components.share.share-community-qr-code.view
  (:require [clojure.set]
            [oops.core :as oops]
            [quo.components.share.qr-code.view :as qr-code]
            [quo.components.share.share-community-qr-code.schema :as component-schema]
            [quo.components.share.share-community-qr-code.style :as style]
            [quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [schema.core :as schema]))

(defn- share-qr-code
  [{:keys [qr-image-uri component-width
           profile-picture emoji]}]
  [:<>
   [rn/view {:style style/content-container}
    [rn/view {:style style/share-qr-container}
     [rn/view {:style style/share-qr-inner-container}]]
    [quo.theme/provider {:theme :light}
     [qr-code/view
      {:qr-image-uri    qr-image-uri
       :size            (style/qr-code-size component-width)
       :avatar          :community
       :profile-picture profile-picture
       :emoji           emoji}]]]])

(defn- view-internal
  [props]
  (reagent/with-let [component-width     (reagent/atom nil)
                     container-component [rn/view {:background-color style/overlay-color}]]
    [quo.theme/provider {:theme :dark}
     [rn/view
      {:accessibility-label :share-qr-code
       :style               style/outer-container
       :on-layout           #(reset! component-width (oops/oget % "nativeEvent.layout.width"))}
      (conj container-component
            (when @component-width
              [share-qr-code
               (-> props
                   (assoc :component-width @component-width)
                   (clojure.set/rename-keys {:type :share-qr-type}))]))]]))

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal component-schema/?schema)))
