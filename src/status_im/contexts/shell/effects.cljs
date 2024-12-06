(ns status-im.contexts.shell.effects
  (:require
    [react-native.reanimated :as reanimated]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.contexts.shell.state :as state]
    [status-im.contexts.shell.utils :as utils]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.shell/change-tab
 (fn [stack-id]
   (when (and (not= stack-id @state/selected-stack-id-value)
              (some #(= stack-id %) shell.constants/stacks-ids))
     (some-> @state/selected-stack-id-shared-value
             (reanimated/set-shared-value (name stack-id)))
     (utils/change-selected-stack stack-id)
     (when-not (= stack-id :wallet-stack)
       (rf/dispatch [:wallet/reset-selected-networks])))))
