(ns status-im.waku.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.node.core :as node]
            [status-im.utils.fx :as fx]))

(defn enabled? [cofx]
  (get-in cofx [:db :multiaccount :waku-enabled]))

(fx/defn switch-waku-enabled
  {:events [:multiaccounts.ui/waku-enabled-switched]}
  [cofx enabled?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update :waku-enabled enabled?
                                                      {})
            (node/prepare-new-config
             {:on-success #(re-frame/dispatch [:logout])})))

(fx/defn switch-waku-bloom-filter-mode
  {:events [:multiaccounts.ui/waku-bloom-filter-mode-switched]}
  [cofx enabled?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :waku-bloom-filter-mode enabled?
             {})
            (node/prepare-new-config
             {:on-success #(re-frame/dispatch [:logout])})))

