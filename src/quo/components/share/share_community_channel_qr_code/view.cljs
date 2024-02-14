(ns quo.components.share.share-community-channel-qr-code.view
  (:require [clojure.set]
            [oops.core :as oops]
            [quo.components.share.qr-code.view :as qr-code]
            [quo.components.share.share-community-channel-qr-code.schema :as component-schema]
            [quo.components.share.share-community-channel-qr-code.style :as style]
            [quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [schema.core :as schema]))

(defn- share-qr-code
  [{:keys [qr-image-uri component-width emoji customization-color full-name]}]
  [rn/view {:style style/content-container}
   [quo.theme/provider {:theme :light}
    [qr-code/view
     {:qr-image-uri        qr-image-uri
      :size                (style/qr-code-size component-width)
      :avatar              :channel
      :emoji               emoji
      :full-name           full-name
      :customization-color customization-color}]]])

(defn- view-internal
  [props]
  (reagent/with-let [component-width     (reagent/atom nil)
                     container-component [rn/view {:style style/container-component}]]
    [quo.theme/provider {:theme :dark}
     [rn/view
      {:accessibility-label :share-qr-code
       :style               style/outer-container
       :on-layout           #(reset! component-width (oops/oget % "nativeEvent.layout.width"))}
      (conj container-component
            (when @component-width
              [share-qr-code props]))]]))

(def view

  (schema/instrument #'view-internal component-schema/?schema))
