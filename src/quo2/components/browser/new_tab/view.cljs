(ns quo2.components.browser.new-tab.view
  (:require [quo2.components.browser.new-tab.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.components.buttons.button.view :as button]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn view-internal
  [{:keys [customization-color theme]}]
  [rn/view
   {:style (style/container customization-color theme)}
   [button/button
    {:type                :primary
     :hit-slop            32
     :on-press            #(js/alert "TODO: to be implemented")
     :size                32
     :icon-only?          true
     :accessibility-label :new-tab
     :customization-color customization-color}
    :i/add]
   [rn/view {:style style/gap}]
   [text/text
    {:size            :paragraph-2
     :weight          :medium
     :number-of-lines 1}
    (i18n/label :t/new-tab)]])

(def view (theme/with-theme view-internal))
