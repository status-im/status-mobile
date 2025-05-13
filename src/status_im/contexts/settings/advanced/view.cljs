(ns status-im.contexts.settings.advanced.view
  (:require [clojure.string :as string]
            [legacy.status-im.fleet.core :as fleets]
            [quo.core :as quo]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.events-helper :as events.helper]
            [status-im.config :as config]
            [status-im.contexts.settings.advanced.events]
            [status-im.contexts.settings.advanced.style :as style]
            [status-im.feature-flags :as ff]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- static-header
  []
  [rn/view {:style style/header}
   [quo/page-nav
    {:background :blur
     :icon-name  :i/arrow-left
     :on-press   events.helper/navigate-back}]])

(defn- advanced-item
  [{:keys [label data] :as _item-data}]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [quo/category
     {:container-style     style/advanced-item
      :label               label
      :customization-color customization-color
      :data                data
      :blur?               true
      :list-type           :settings}]))

(defn- format-timestamp
  [prefix-string last-backup-timestamp]
  (-> (str prefix-string " " (datetime/timestamp->relative (* 1000 last-backup-timestamp)))
      (string/capitalize)
      (string/replace #"am|pm" {"am" "AM" "pm" "PM"})))

(defn- waku-backup-settings
  []
  (let [{:keys [backup-enabled?
                last-backup]} (rf/sub [:profile/profile])
        performing-backup?    (rf/sub [:backup/performing-backup])
        customization-color   (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/waku-backup)}]
     (comment
       [quo/text
        {:style {:padding-horizontal 20
                 :margin-bottom      16}}
        "Explanation and implications of the toggle"])
     [rn/view {:style style/waku-backup-toggle}
      [quo/drawer-action
       {:title               (i18n/label :t/waku-backup-enabled)
        :action              :toggle
        :state               (when backup-enabled? :selected)
        :blur?               true
        :accessibility-label :waku-backup-switch
        :icon                :i/revert
        :customization-color customization-color
        :on-press            #(rf/dispatch [:advanced-settings/toggle-waku-backup])}]]
     [quo/bottom-actions
      {:actions          :one-action
       :blur?            true
       :description      :bottom
       :description-text (if performing-backup?
                           (i18n/label :t/backing-up)
                           (format-timestamp (i18n/label :t/last-backup-performed) last-backup))
       :button-one-label (i18n/label :t/perform-backup)
       :button-one-props {:customization-color customization-color
                          :on-press            #(rf/dispatch [:advanced-settings/perform-backup])
                          :disabled?           performing-backup?}}]]))

(defn- option-picker-sheet
  [{:keys [title options option-selected? option-title-fn on-press]}]
  (let [option-xf (map (fn [option]
                         [quo/drawer-action
                          {:title    (option-title-fn option)
                           :state    (when (option-selected? option) :selected)
                           :blur?    true
                           :on-press #(on-press option)}]))]
    [:<>
     [quo/drawer-top {:title title}]
     (into [rn/view {:style style/option-picker-sheet}]
           option-xf
           options)]))

(def ^:private log-levels
  "The status-go log library (zap) doesn't support the trace level, so we remove
  the trace option from the UI. When trace is enabled, an error will happen while
  trying to log in (the user will see the error 'wrong password')."
  {""      "DISABLED"
   "ERROR" "ERROR"
   "WARN"  "WARN"
   "INFO"  "INFO"
   "DEBUG" "DEBUG"})

(defn- log-level-sheet
  []
  (let [current-log-level (rf/sub [:log-level/current-profile-log-level])]
    [option-picker-sheet
     {:title            (i18n/label :t/log-level)
      :options          (keys log-levels)
      :option-selected? (fn [log-level-id]
                          (= (log-levels log-level-id) current-log-level))
      :option-title-fn  #(-> % log-levels string/capitalize)
      :on-press         (fn [log-level-id]
                          (rf/dispatch [:advanced-settings/change-log-level log-level-id])
                          (rf/dispatch [:hide-bottom-sheet]))}]))

(defn- fleet-sheet
  []
  (let [current-fleet (rf/sub [:fleets/current-fleet])]
    [option-picker-sheet
     {:title            (i18n/label :t/fleet)
      :options          (map keyword fleets/fleets)
      :option-selected? (fn [fleet]
                          (= fleet current-fleet))
      :option-title-fn  name
      :on-press         (fn [fleet]
                          (rf/dispatch [:advanced-settings/change-fleet fleet]))}]))

(defn- copy-string-callback
  [content property-copied]
  (fn []
    (clipboard/set-string (str content))
    (rf/dispatch
     [:toasts/upsert
      {:id   :string-copied
       :type :positive
       :text (str property-copied " " (i18n/label :t/copied-to-clipboard))}])))

(defn- open-waku-settings
  []
  (rf/dispatch [:show-bottom-sheet
                {:content waku-backup-settings
                 :shell?  true
                 :blur?   true
                 :theme   :dark}]))

(defn- toggle-peer-syncing
  []
  (rf/dispatch [:profile.settings/toggle-peer-syncing]))

(defn- open-log-level-sheet
  []
  (rf/dispatch [:show-bottom-sheet
                {:content log-level-sheet
                 :theme   :dark
                 :shell?  true
                 :blur?   true}]))

(defn- open-fleet-sheet
  []
  (rf/dispatch [:show-bottom-sheet
                {:content fleet-sheet
                 :theme   :dark
                 :shell?  true
                 :blur?   true}]))

(defn- force-crash
  []
  (rf/dispatch [:app-monitoring/intended-panic "status-mobile intentional panic"]))

(defn- toggle-light-client
  []
  (rf/dispatch [:advanced-settings/toggle-light-client]))

(defn- get-options
  [{:keys [log-level backup-enabled? last-backup peers-count peer-syncing-enabled?
           current-mailserver light-client-enabled? current-fleet analytics-user-id]}]
  [{:label (i18n/label :t/syncing)
    :data  [{:title               (i18n/label :t/waku-backup)
             :accessibility-label :backup-settings-button
             :on-press            open-waku-settings
             :description         :text
             :action              :arrow
             :label               :text
             :label-props         (if backup-enabled?
                                    (i18n/label :t/backup-enabled)
                                    (i18n/label :t/backup-disabled))
             :description-props   {:text (format-timestamp (i18n/label :t/latest) last-backup)}}
            {:title               (i18n/label :t/peer-syncing)
             :accessibility-label :peer-syncing
             :action              :selector
             :action-props        {:on-change toggle-peer-syncing
                                   :checked?  peer-syncing-enabled?}}
            {:title             (i18n/label :t/history-nodes)
             :on-press          (copy-string-callback current-mailserver (i18n/label :t/history-nodes))
             :description       :text
             :description-props {:text current-mailserver}}]}
   {:label (i18n/label :t/debugging)
    :data  [{:title               (i18n/label :t/log-level)
             :accessibility-label :log-level-settings-button
             :on-press            open-log-level-sheet
             :action              :arrow
             :label               :text
             :label-props         (some-> log-level
                                          log-levels
                                          string/capitalize)}
            {:title               (i18n/label :t/fleet)
             :accessibility-label :fleet-settings-button
             :on-press            open-fleet-sheet
             :action              :arrow
             :label               :text
             :label-props         current-fleet}
            {:title               (i18n/label :t/peers-stats)
             :accessibility-label :peers-stats
             :description         :text
             :description-props   {:text (str (i18n/label :t/peers-count) ": " peers-count)}
             :on-press            (copy-string-callback peers-count (i18n/label :t/peers-count))}
            (when (ff/enabled? ::ff/analytics.copy-user-id)
              {:title               "Copy analytics user ID"
               :accessibility-label :copy-analytics-user-id
               :on-press            (copy-string-callback analytics-user-id
                                                          "Analytics user ID")})
            (when (ff/enabled? ::ff/app-monitoring.intentional-crash)
              {:size                :small
               :title               (str "Force crash immediately"
                                         (when (string/blank? config/sentry-dsn-status-go)
                                           " (Sentry DSN is not set)"))
               :accessibility-label :intended-panic
               :on-press            force-crash})]}
   {:label (i18n/label :t/other)
    :data  [{:title               (i18n/label :t/light-client-enabled)
             :accessibility-label :light-client-enabled
             :action              :selector
             :action-props        {:on-change toggle-light-client
                                   :checked?  light-client-enabled?}}]}])

(defn view
  []
  (let [log-level             (rf/sub [:log-level/current-profile-log-level])
        {:keys [backup-enabled?
                last-backup]} (rf/sub [:profile/profile])
        peers-count           (rf/sub [:peer-stats/count])
        peer-syncing-enabled? (rf/sub [:profile/peer-syncing-enabled?])
        current-mailserver    (rf/sub [:mailserver/current-name])
        light-client-enabled? (rf/sub [:profile/light-client-enabled?])
        current-fleet         (rf/sub [:fleets/current-fleet])
        analytics-user-id     (rf/sub [:centralized-metrics/user-id])
        options               (rn/use-memo
                               (fn []
                                 (get-options {:log-level             log-level
                                               :backup-enabled?       backup-enabled?
                                               :last-backup           last-backup
                                               :peers-count           peers-count
                                               :peer-syncing-enabled? peer-syncing-enabled?
                                               :current-mailserver    current-mailserver
                                               :light-client-enabled? light-client-enabled?
                                               :current-fleet         current-fleet
                                               :analytics-user-id     analytics-user-id}))
                               [backup-enabled? last-backup log-level peers-count
                                peer-syncing-enabled?])]
    (rn/use-mount #(rf/dispatch [:peer-stats/get-count]))
    [quo/overlay {:type :shell}
     [static-header]
     [rn/flat-list
      {:header                          [quo/page-top {:title (i18n/label :t/advanced)}]
       :data                            options
       :render-fn                       advanced-item
       :content-container-style         {:padding-bottom safe-area/bottom}
       :shows-vertical-scroll-indicator false}]]))
