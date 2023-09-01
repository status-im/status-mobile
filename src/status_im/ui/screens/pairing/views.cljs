(ns status-im.ui.screens.pairing.views
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.pairing.styles :as styles]))

(def syncing (reagent/atom false))
(def installation-name (reagent/atom ""))

(defn icon-style
  [style]
  style)

(defn synchronize-installations!
  []
  (reset! syncing true)
  ;; Currently we don't know how long it takes, so we just disable for 10s, to avoid spamming
  (js/setTimeout #(reset! syncing false) 10000)
  (re-frame/dispatch [:pairing.ui/synchronize-installation-pressed]))

(defn pair!
  []
  (re-frame/dispatch [:pairing.ui/pair-devices-pressed]))

(defn enable-installation!
  [installation-id]
  (re-frame/dispatch [:pairing.ui/enable-installation-pressed installation-id]))

(defn disable-installation!
  [installation-id]
  (re-frame/dispatch [:pairing.ui/disable-installation-pressed installation-id]))

(defn toggle-enabled!
  [installation-id enabled? _]
  (if enabled?
    (disable-installation! installation-id)
    (enable-installation! installation-id)))

(defn footer
  []
  [react/touchable-highlight
   {:on-press (when-not @syncing
                synchronize-installations!)
    :style    {:height 52}}
   [react/view
    {:style styles/footer-content}
    [react/text
     {:style styles/footer-text}
     (if @syncing
       (i18n/label :t/syncing-devices)
       (i18n/label :t/sync-all-devices))]]])

(defn pair-this-device
  []
  [react/touchable-highlight
   {:on-press pair!
    :style    styles/pair-this-device}
   [react/view {:style styles/pair-this-device-actions}
    [react/view
     [react/view (styles/pairing-button true)
      [icons/icon :main-icons/add (icon-style (styles/pairing-button-icon true))]]]
    [react/view {:style styles/pairing-actions-text}
     [react/view
      [react/text
       {:style               styles/pair-this-device-title
        :accessibility-label :advertise-device} (i18n/label :t/pair-this-device)]]
     [react/view
      [react/text (i18n/label :t/pair-this-device-description)]]]]])

(defn your-device
  [{:keys [installation-id name device-type]}]
  [quo/list-item
   {:icon  (if (= "desktop"
                  device-type)
             :main-icons/desktop
             :main-icons/mobile)
    :title (str name " (" (i18n/label :t/you) ", " (subs installation-id 0 5) ")")}])

(defn render-row
  [{:keys [name
           enabled?
           device-type
           installation-id]}]
  [quo/list-item
   {:icon      (if (= "desktop" device-type)
                 :main-icons/desktop
                 :main-icons/mobile)
    :title     (str (if (string/blank? name)
                      (i18n/label :t/pairing-no-info)
                      name)
                    " ("
                    (subs installation-id 0 5)
                    ")")
    :accessory :checkbox
    :active    enabled?
    :on-press  (partial toggle-enabled! installation-id enabled?)}])

(defn render-rows
  [installations]
  [react/scroll-view {:style styles/wrapper}
   [your-device (first installations)]
   (when (seq (rest installations))
     [list/flat-list
      {:data               (rest installations)
       :default-separator? false
       :key-fn             :installation-id
       :render-fn          render-row}])])

(views/defview edit-installation-name
  []
  [react/keyboard-avoiding-view styles/edit-installation
   [react/scroll-view {:keyboard-should-persist-taps :handled}
    [react/view
     [quo/text-input
      {:placeholder         (i18n/label :t/specify-name)
       :label               (i18n/label :t/pairing-please-set-a-name)
       :accessibility-label :device-name
       :default-value       @installation-name
       :on-change-text      #(reset! installation-name %)
       :auto-focus          true}]]]
   [react/view styles/bottom-container
    [react/view {:flex 1}]
    [quo/button
     {:type     :secondary
      :after    :main-icon/next
      :disabled (string/blank? @installation-name)
      :on-press #(do
                   (re-frame/dispatch [:pairing.ui/set-name-pressed @installation-name])
                   (reset! installation-name ""))}
     (i18n/label :t/continue)]]])

(defn info-section
  []
  [react/view {:style styles/info-section}
   [react/touchable-highlight
    {:on-press #(.openURL ^js react/linking "https://status.im/user_guides/pairing_devices.html")}
    [react/text {:style styles/info-section-text} (i18n/label :t/learn-more)]]])

(defn installations-list
  [installations]
  [react/view {:style styles/installation-list}
   [react/view {:style styles/paired-devices-title}
    [react/text (i18n/label :t/paired-devices)]]
   (render-rows installations)])

(views/defview installations
  []
  (views/letsubs [installs [:pairing/installations]]
    [:<>
     [react/scroll-view
      (if (string/blank? (-> installs first :name))
        [edit-installation-name]
        [react/view
         [pair-this-device]
         [info-section]
         [installations-list installs]])]
     (when (seq installs) [footer])]))
