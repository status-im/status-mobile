(ns status-im.common.languages
  (:require
    [quo.core :as quo]
    [react-native.mmkv :as mmkv]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.setup.i18n-resources :as i18n-resources]))

(def descriptor
  [{:key     :language
    :type    :select
    :options (mapv (fn [lang] {:key lang}) i18n-resources/languages)}])

(defn view
  []
  (let [state (reagent/atom {:language i18n-resources/default-device-language})]
    (fn []
      (mmkv/set "language" (name (:language @state)))
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/counter @state (:value @state)]])))
