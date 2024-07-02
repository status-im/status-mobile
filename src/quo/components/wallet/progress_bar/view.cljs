(ns quo.components.wallet.progress-bar.view
  (:require
    [quo.components.wallet.progress-bar.schema :as progress-bar-schema]
    [quo.components.wallet.progress-bar.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- view-internal
  [{:keys [full-width?] :as props}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:accessibility-label :progress-bar
      :style               (style/root-container props theme)}
     (when full-width?
       [rn/view {:style (style/progressed-bar props theme)}])]))

(def view (schema/instrument #'view-internal progress-bar-schema/?schema))
