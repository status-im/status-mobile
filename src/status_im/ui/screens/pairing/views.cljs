(ns status-im.ui.screens.pairing.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.utils.platform :as utils.platform]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.checkbox.view :as checkbox.views]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.screens.pairing.styles :as styles]
            [status-im.ui.components.topbar :as topbar]))

(def syncing (reagent/atom false))
(def installation-name (reagent/atom ""))

(defn icon-style [{:keys [width height] :as style}]
  (if utils.platform/desktop?
    {:container-style {:width width

                       :height height}
     :style  style}
    style))

(defn synchronize-installations! []
  (reset! syncing true)
  ;; Currently we don't know how long it takes, so we just disable for 10s, to avoid
  ;; spamming
  (js/setTimeout #(reset! syncing false) 10000)
  (re-frame/dispatch [:pairing.ui/synchronize-installation-pressed]))

(defn pair! []
  (re-frame/dispatch [:pairing.ui/pair-devices-pressed]))

(defn enable-installation! [installation-id]
  (re-frame/dispatch [:pairing.ui/enable-installation-pressed installation-id]))

(defn disable-installation! [installation-id]
  (re-frame/dispatch [:pairing.ui/disable-installation-pressed installation-id]))

(defn toggle-enabled! [installation-id enabled? _]
  (if enabled?
    (disable-installation! installation-id)
    (enable-installation! installation-id)))

(defn footer [syncing]
  [react/touchable-highlight {:on-press (when-not @syncing
                                          synchronize-installations!)
                              :style    {:height         52
                                         :elevation      8
                                         :shadow-radius  4
                                         :shadow-offset  {:width 0 :height -5}
                                         :shadow-opacity 0.3
                                         :shadow-color   "rgba(0, 9, 26, 0.12)"}}
   [react/view
    {:style styles/footer-content}
    [react/text
     {:style styles/footer-text}
     (if @syncing
       (i18n/label :t/syncing-devices)
       (i18n/label :t/sync-all-devices))]]])

(defn pair-this-device []
  [react/touchable-highlight {:on-press pair!
                              :style styles/pair-this-device}
   [react/view {:style styles/pair-this-device-actions}
    [react/view
     [react/view (styles/pairing-button true)
      [icons/icon :main-icons/add (icon-style (styles/pairing-button-icon true))]]]
    [react/view {:style styles/pairing-actions-text}
     [react/view
      [react/text {:style               styles/pair-this-device-title
                   :accessibility-label :advertise-device} (i18n/label :t/pair-this-device)]]
     [react/view
      [react/text (i18n/label :t/pair-this-device-description)]]]]])

(defn sync-devices []
  [react/touchable-highlight {:on-press synchronize-installations!
                              :style styles/pair-this-device}
   [react/view {:style styles/pair-this-device-actions}
    [react/view
     [react/view (styles/pairing-button true)
      [icons/icon :main-icons/wnode (icon-style (styles/pairing-button-icon true))]]]
    [react/view {:style styles/pairing-actions-text}
     [react/view
      [react/text {:style styles/pair-this-device-title}
       (if @syncing
         (i18n/label :t/syncing-devices)
         (i18n/label :t/sync-all-devices))]]]]])

(defn your-device [{:keys [installation-id name device-type]}]
  [react/view {:style styles/installation-item}
   [react/view {:style (styles/pairing-button true)}
    [icons/icon  (if (= "desktop"
                        device-type)
                   :main-icons/desktop
                   :main-icons/mobile)

     (icon-style (styles/pairing-button-icon true))]]
   [react/view {:style styles/pairing-actions-text}
    [react/view
     [react/text (str
                  name
                  " ("
                  (i18n/label :t/you)
                  ", "
                  (subs installation-id 0 5)
                  ")")]]]])

(defn render-row [{:keys [name
                          device-type
                          enabled?
                          installation-id]}]
  [react/touchable-highlight
   {:accessibility-label :installation-item}
   [react/view {:style styles/installation-item}
    [react/view {:style (styles/pairing-button enabled?)}
     [icons/icon (if (= "desktop"
                        device-type)
                   :main-icons/desktop
                   :main-icons/mobile)
      (icon-style (styles/pairing-button-icon enabled?))]]
    [react/view {:style styles/pairing-actions-text}
     [react/view
      [react/text (str
                   (if (string/blank? name)
                     (i18n/label :t/pairing-no-info)
                     name)
                   " ("
                   (subs installation-id 0 5)
                   ")")]]]
    [react/view
     (if utils.platform/ios?
       ;; On IOS switches seems to be broken, they take up value of dev-mode? (so if dev mode is on they all show to be on).
       ;; Replacing therefore with checkbox until I have more time to investigate
       (checkbox.views/checkbox {:checked? enabled?
                                 :on-value-change (partial toggle-enabled! installation-id enabled?)})
       [react/switch {:track-color     #js {:true colors/blue :false nil}
                      :value           enabled?
                      :on-value-change (partial toggle-enabled! installation-id enabled?)}])]]])

(defn render-rows [installations]
  [react/scroll-view {:style styles/wrapper}
   [your-device (first installations)]
   (when (seq (rest installations))
     [list/flat-list {:data               (rest installations)
                      :default-separator? false
                      :key-fn             :installation-id
                      :render-fn          render-row}])])

(views/defview edit-installation-name []
  [react/keyboard-avoiding-view styles/edit-installation
   [react/scroll-view {:keyboard-should-persist-taps :handled}
    [react/view
     [react/text (i18n/label :t/pairing-please-set-a-name)]]
    [text-input/text-input-with-label
     {:placeholder     (i18n/label :t/specify-name)
      :style               styles/input
      :accessibility-label :device-name
      :container           styles/input-container
      :default-value       @installation-name
      :on-change-text      #(reset! installation-name %)
      :auto-focus          true}]]
   [react/view styles/bottom-container
    [react/view components.styles/flex]
    [components.common/bottom-button
     {:forward?  true
      :label     (i18n/label :t/continue)
      :disabled? (string/blank? @installation-name)
      :on-press  #(do
                    (re-frame/dispatch [:pairing.ui/set-name-pressed @installation-name])
                    (reset! installation-name ""))}]]])

(defn info-section []
  [react/view {:style styles/info-section}
   [react/touchable-highlight {:on-press #(.openURL react/linking "https://status.im/tutorials/pairing.html")}
    [react/text {:style styles/info-section-text} (i18n/label :t/learn-more)]]])

(defn installations-list [installations]
  [react/view {:style styles/installation-list}
   [react/view {:style styles/paired-devices-title}
    [react/text (i18n/label :t/paired-devices)]]
   (render-rows installations)])

(views/defview installations []
  (views/letsubs [installations [:pairing/installations]]
    [react/view {:flex 1}
     [topbar/topbar {:title :t/devices}]
     [react/scroll-view {:style {:background-color :white}}
      (if (string/blank? (-> installations first :name))
        [edit-installation-name]
        [react/view
         [pair-this-device]
         [info-section]
         [installations-list installations]])]
     (when (seq installations) [footer syncing])]))
