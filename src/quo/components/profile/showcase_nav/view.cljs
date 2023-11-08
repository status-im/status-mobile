(ns quo.components.profile.showcase-nav.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.profile.showcase-nav.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- render-button
  [{:keys [icon id]} index _ {:keys [state on-press active-id]}]
  (let [active?       (= id active-id)
        button-type   (if active? :grey :ghost)
        scroll-state? (= state :scroll)]
    [button/button
     {:accessibility-label :showcase-nav-item
      :size                32
      :type                button-type
      :background          (when scroll-state? :blur)
      :icon-only?          true
      :on-press            (fn []
                             (when on-press
                               (on-press id index)))
      :container-style     style/button-container}
     icon]))

(defn- view-internal
  [{:keys [theme container-style default-active state data on-press active-id]}]
  [rn/view
   {:style               container-style
    :accessibility-label :showcase-nav}
   [rn/flat-list
    {:data                              data
     :key-fn                            :id
     :horizontal                        true
     :shows-horizontal-scroll-indicator false
     :content-container-style           (style/container state theme)
     :render-fn                         render-button
     :render-data                       {:state     state
                                         :on-press  on-press
                                         :active-id (or active-id default-active)}}]])

(def view (quo.theme/with-theme view-internal))
