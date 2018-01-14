(ns status-im.ui.screens.dev.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.react :refer [picker
                                                   scroll-view
                                                   text
                                                   touchable-highlight
                                                   view]]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.dev.styles :as styles]))

(defn check-box [checked?]
  [view (styles/check-box checked?)
   (when checked?
     [vi/icon :icons/ok {:style styles/check-icon}])])

(defview toggle-setting [title id]
  (letsubs [settings [:dev-settings]]
    [touchable-highlight {:on-press #(dispatch [:toggle-dev-setting id])}
     [view styles/setting-container
      [text {:style styles/setting-text}
        title]
      [check-box (get settings id)]]]))

(def log-levels
  [{:label "Error" :value "error"}
   {:label "Warn"  :value "warn"}
   {:label "Info"  :value "info"}
   {:label "Debug" :value "debug"}
   {:label "Trace" :value "trace"}])

(defview select-log-level []
  (letsubs [{:keys [log-level]} [:dev-settings]]
    [view styles/setting-container
     [view styles/select-log-level-container
      [text {:style styles/setting-text}
       "Log level"]
      [picker {:selected log-level
               :on-change #(dispatch [:set-dev-setting :log-level %])}
       log-levels]]]))

(defn dev-settings []
  [view
   [status-bar]
   [toolbar/toolbar {:modal? false} toolbar/default-nav-back
    [toolbar/content-title "Dev Settings"]]
   [scroll-view
    [toggle-setting "TestFairy" :testfairy-enabled?]
    [toggle-setting "Stub status-go" :stub-status-go?]
    [toggle-setting "Mainnet networks" :mainnet-networks-enabled?]
    [toggle-setting "ERC20" :erc20-enabled?]
    [toggle-setting "Offline inbox" :offline-inbox-enabled?]

    [select-log-level]

    [toggle-setting "JSC" :jsc-enabled?]
    [toggle-setting "Queue message" :queue-message-enabled?]]])
