(ns quo.components.settings.page-setting.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.selectors.selectors.view :as selectors]
    [quo.components.settings.page-setting.style :as style]
    [quo.theme]
    [react-native.core :as rn]))

(defn page-setting
  [{:keys [setting-text customization-color checked? container-style on-change disabled?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:style (merge (style/container theme)
                    container-style)}
     [text/text
      {:weight          :medium
       :number-of-lines 1} setting-text]
     [selectors/view
      {:type                :toggle
       :checked?            checked?
       :customization-color customization-color
       :accessibility-label :user-list-toggle-check
       :disabled?           disabled?
       :on-change           (when on-change on-change)}]]))
