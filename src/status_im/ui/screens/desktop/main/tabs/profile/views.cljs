(ns status-im.ui.screens.desktop.main.tabs.profile.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]
            [status-im.utils.logging.core :as logging]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [taoensso.timbre :as log]
            [status-im.utils.gfycat.core :as gfy]
            [clojure.string :as string]
            [status-im.ui.screens.offline-messaging-settings.views :as offline-messaging.views]
            [status-im.ui.screens.pairing.views :as pairing.views]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.screens.desktop.main.tabs.profile.styles :as styles]
            [status-im.ui.screens.pairing.styles :as pairing.styles]
            [status-im.ui.screens.profile.user.views :as profile]
            [status-im.ui.screens.profile.seed.views :as profile.recovery]
            [status-im.ui.components.common.common :as components.common]))

(defn profile-badge [{:keys [name photo-path public-key]} editing?]
  [react/view styles/profile-badge
   [react/image {:source {:uri photo-path}
                 :style  styles/profile-photo}]
   (if editing?
     [react/text-input {:default-value name
                        :placeholder   ""
                        :auto-focus    true
                        :font          :medium
                        :style         styles/profile-editing-user-name
                        :on-change     #(re-frame/dispatch [:my-profile/update-name
                                                            (.-text (.-nativeEvent %))])}]
     [react/text {:style           styles/profile-user-name
                  :font           :medium
                  :number-of-lines 1}
      name])
   (let [gfy-name (gfy/generate-gfy public-key)]
     (when (and (not= gfy-name gfy/unknown-gfy)
                (not= gfy-name name))
       [react/text {:style           styles/profile-three-words
                    :number-of-lines 1}
        gfy-name]))])

(views/defview copied-tooltip [opacity]
  (views/letsubs []
    [react/view {:style (styles/tooltip-container opacity)}
     [react/view {:style styles/tooltip-icon-text}
      [vector-icons/icon :main-icons/check
       {:style styles/check-icon}]
      [react/text {:style {:font-size 14 :color colors/green}}
       (i18n/label :sharing-copied-to-clipboard)]]
     [react/view {:style styles/tooltip-triangle}]]))

(views/defview qr-code []
  (views/letsubs [{:keys [public-key]} [:account/account]
                  tooltip-opacity      [:get-in [:tooltips :qr-copied]]]
    [react/view
     [react/view {:style styles/qr-code-container}
      [react/text {:style styles/qr-code-title
                   :font  :medium}
       (string/replace (i18n/label :qr-code-public-key-hint) "\n" "")]
      [react/view {:style styles/qr-code}
       [qr-code-viewer/qr-code {:value public-key :size 130}]]
      [react/view {:style {:align-items :center}}
       [react/text {:style            styles/qr-code-text
                    :selectable       true
                    :selection-color  colors/blue}
        public-key]
       (when tooltip-opacity
         [copied-tooltip tooltip-opacity])]
      [react/touchable-highlight {:on-press #(do
                                               (re-frame/dispatch [:copy-to-clipboard public-key])
                                               (re-frame/dispatch [:show-tooltip :qr-copied]))}
       [react/view {:style styles/qr-code-copy}
        [react/text {:style styles/qr-code-copy-text}
         (i18n/label :copy-qr)]]]]]))

(defn installations-section [your-installation-id
                             your-installation-name
                             installations]
  [react/view
   (if (string/blank? your-installation-name)
     [pairing.views/edit-installation-name]
     [react/view
      [pairing.views/pair-this-device]
      [pairing.views/info-section]
      [pairing.views/sync-devices]
      [react/view {:style pairing.styles/installation-list}
       [pairing.views/your-device your-installation-id your-installation-name]
       (for [installation installations]
         ^{:key (:installation-id installation)}
         [react/view {:style {:margin-bottom 10}}
          (pairing.views/render-row installation)])]])])

(defn connection-status
  "generates a composite message of the current connection state given peer and mailserver statuses"
  [peers-count node-status mailserver-state peers-disconnected?]
  ;; TODO probably not ideal criteria for searching
  ;; ask about directly calling rpc method to find discovery.started
  (let [searching?            (= :starting node-status)
        peers-connected?      (not peers-disconnected?)
        mailserver-connected? (= :connected mailserver-state)]
    (cond
      (and peers-connected? searching?)                  "Connected and searching"
      (and peers-connected? (not mailserver-connected?)) (str "Connected with " peers-count " peers")
      (and peers-connected? mailserver-connected?)       (str "Connected with " peers-count " peers including mailserver.")
      (and peers-disconnected? searching?)               "Disconnected and searching"
      :else                                              "Disconnected")))

(defn connection-statistics-display
  [{:keys [mailserver-request-process-time
           mailserver-request-errors
           les-packets-in
           les-packets-out
           p2p-inbound-traffic
           p2p-outbound-traffic]}]
  [react/view {:style {:flex-direction :row}}
   [react/view
    [react/text {:style styles/connection-stats-title}
     "Mailserver requests"]
    [react/text {:style styles/connection-stats-entry}
     (str "errors " mailserver-request-errors)]
    [react/text {:style styles/connection-stats-entry}
     (str "process time " mailserver-request-process-time)]]
   [react/view
    [react/text {:style styles/connection-stats-title}
     "p2p traffic"]
    [react/text {:style styles/connection-stats-entry}
     (str "inbound " p2p-inbound-traffic)]
    [react/text {:style styles/connection-stats-entry}
     (str "outbound " p2p-outbound-traffic)]]
   [react/view
    [react/text {:style styles/connection-stats-title}
     "LES packets"]
    [react/text {:style styles/connection-stats-entry}
     (str "inbound " les-packets-in)]
    [react/text {:style styles/connection-stats-entry}
     (str "outbound " les-packets-out)]]])

(views/defview logging-display []
  (views/letsubs [logging-enabled [:settings/logging-enabled]]
    [react/view
     [react/view {:style (styles/adv-settings-row false)}
      [react/text {:style (assoc (styles/adv-settings-row-text colors/black)
                                 :font-size 14)} (i18n/label :t/logging-enabled)]
      [react/switch {:on-tint-color   colors/blue
                     :value           logging-enabled
                     :on-value-change #(re-frame/dispatch [:log-level.ui/logging-enabled (not logging-enabled)])}]]
     [react/view {:style (styles/adv-settings-row false)}
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:logging.ui/send-logs-pressed])}
       [react/text {:style (styles/adv-settings-row-text colors/red)}
        (i18n/label :t/send-logs)]]]]))

(views/defview advanced-settings []
  (views/letsubs [current-mailserver-id [:mailserver/current-id]
                  {:keys [settings]}    [:account/account]
                  mailservers           [:mailserver/fleet-mailservers]
                  mailserver-state      [:mailserver/state]
                  node-status           [:node-status]
                  peers-count           [:peers-count]
                  connection-stats      [:connection-stats]
                  disconnected          [:disconnected?]]
    (let [render-fn (offline-messaging.views/render-row current-mailserver-id)
          pfs? (:pfs? settings)
          connection-message (connection-status peers-count node-status mailserver-state disconnected)]
      [react/scroll-view
       [react/text {:style styles/advanced-settings-title
                    :font  :medium}
        (i18n/label :advanced-settings)]

       [react/view {:style styles/title-separator}]
       [react/text {:style styles/adv-settings-subtitle} "Connections"]
       [react/view {:style {:flex-direction :row
                            :margin-bottom 8}}
        [react/view {:style (styles/connection-circle disconnected)}]
        [react/text connection-message]]
       (connection-statistics-display connection-stats)

       [react/view {:style styles/title-separator}]
       [react/text {:style styles/adv-settings-subtitle} (i18n/label :offline-messaging)]
       [react/view
        (for [mailserver (vals mailservers)]
          ^{:key (:id mailserver)}
          [react/view {:style {:margin-vertical 8}}
           [render-fn mailserver]])]
       [react/view {:style styles/title-separator}]
       [react/text {:style styles/adv-settings-subtitle} (i18n/label :t/logging)]
       [logging-display]

       [react/view {:style styles/title-separator}]
       [react/text {:style styles/adv-settings-subtitle} (i18n/label :t/pfs)]
       [react/view {:style (styles/profile-row false)}
        [react/text {:style (styles/profile-row-text colors/black)} (i18n/label :t/pfs)]
        [react/switch {:on-tint-color   colors/blue
                       :value           pfs?
                       :on-value-change #(re-frame/dispatch [:accounts.ui/toggle-pfs (not pfs?)])}]]])))

(views/defview installations []
  (views/letsubs [installations     [:pairing/installations]
                  installation-id   [:pairing/installation-id]
                  installation-name [:pairing/installation-name]]
    [react/scroll-view
     (installations-section
      installation-id
      installation-name
      installations)]))

(views/defview backup-recovery-phrase []
  [profile.recovery/backup-seed])

(defn share-contact-code []
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :qr-code])}
   [react/view {:style styles/share-contact-code}
    [react/view {:style styles/share-contact-code-text-container}
     [react/text {:style       styles/share-contact-code-text}
      (i18n/label :share-contact-code)]]
    [react/view {:style               styles/share-contact-icon-container
                 :accessibility-label :share-my-contact-code-button}
     [vector-icons/icon :main-icons/qr {:style {:tint-color colors/blue}}]]]])

(defn help-item [help-open?]
  [react/touchable-highlight {:style    (styles/adv-settings-row help-open?)
                              :on-press #(re-frame/dispatch [:navigate-to (if help-open? :home :help-center)])}
   [react/view {:style styles/adv-settings}
    [react/text {:style (styles/adv-settings-row-text colors/black)
                 :font  (if help-open? :medium :default)}
     (i18n/label  :t/help-center)]
    [vector-icons/icon :main-icons/next {:style {:tint-color colors/gray}}]]])

(defn advanced-settings-item [adv-settings-open?]
  [react/touchable-highlight {:style  (styles/adv-settings-row adv-settings-open?)
                              :on-press #(do
                                           (re-frame/dispatch [:navigate-to (if adv-settings-open? :home :advanced-settings)])
                                           (re-frame/dispatch [:load-debug-metrics]))}
   [react/view {:style styles/adv-settings}
    [react/text {:style (styles/adv-settings-row-text colors/black)
                 :font  (if adv-settings-open? :medium :default)}
     (i18n/label :t/advanced-settings)]
    [vector-icons/icon :main-icons/next {:style {:tint-color colors/gray}}]]])

(views/defview profile [{:keys [seed-backed-up? mnemonic] :as user}]
  (views/letsubs [current-view-id [:get :view-id]
                  editing?        [:get :my-profile/editing?]] ;; TODO janherich: refactor my-profile, unnecessary complicated structure in db (could be just `:staged-name`/`:editing?` fields in account map) and horrible way to access it woth `:get`/`:set` subs/events
    (let [adv-settings-open?           (= current-view-id :advanced-settings)
          help-open?                   (= current-view-id :help-center)
          installations-open?          (= current-view-id :installations)
          backup-recovery-phrase-open? (= current-view-id :backup-recovery-phrase)
          notifications?               (get-in user [:desktop-notifications?])
          show-backup-seed?            (and (not seed-backed-up?) (not (string/blank? mnemonic)))]
      [react/view
       [react/view {:style styles/profile-edit}
        [react/touchable-highlight {:on-press #(re-frame/dispatch (if editing?
                                                                    [:my-profile/save-profile]
                                                                    [:my-profile/start-editing-profile]))}
         [react/text {:style {:color colors/blue}}
          (i18n/label (if editing? :t/done :t/edit))]]]
       [react/view styles/profile-view
        [profile-badge user editing?]
        [share-contact-code]
        [react/view {:style (styles/profile-row false)}
         [react/text {:style (styles/profile-row-text colors/black)} (i18n/label :notifications)]
         [react/switch {:on-tint-color   colors/blue
                        :value           notifications?
                        :on-value-change #(re-frame/dispatch [:accounts.ui/notifications-enabled (not notifications?)])}]]
        [advanced-settings-item adv-settings-open?]
        [help-item help-open?]
        [react/touchable-highlight {:style  (styles/profile-row installations-open?)
                                    :on-press #(re-frame/dispatch [:navigate-to (if installations-open? :home :installations)])}
         [react/view {:style styles/adv-settings}
          [react/text {:style (styles/profile-row-text colors/black)}
           (i18n/label :t/devices)]
          [vector-icons/icon :main-icons/next {:style {:tint-color colors/gray}}]]]
        (when show-backup-seed?
          [react/touchable-highlight {:style  (styles/profile-row backup-recovery-phrase-open?)
                                      :on-press #(re-frame/dispatch [:navigate-to :backup-recovery-phrase])}
           [react/view {:style styles/adv-settings}
            [react/text {:style (styles/profile-row-text colors/black)
                         :font  (if backup-recovery-phrase-open? :medium :default)}
             (i18n/label :wallet-backup-recovery-title)]
            [components.common/counter {:size 22} 1]]])
        [react/view {:style (styles/profile-row false)}
         [react/touchable-highlight {:on-press #(re-frame/dispatch [:accounts.logout.ui/logout-confirmed])}
          [react/text {:style (styles/profile-row-text colors/red)} (i18n/label :t/logout)]]
         [react/view [react/text {:style (styles/profile-row-text colors/gray)} "V" build/version " (" build/commit-sha ")"]]]]])))

(views/defview profile-data []
  (views/letsubs
    [user [:account/account]]
    {:component-will-unmount
     #(re-frame/dispatch [:set :my-profile/editing? false])}
    [profile user]))
