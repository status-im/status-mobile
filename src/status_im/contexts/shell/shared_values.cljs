(ns status-im.contexts.shell.shared-values
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.contexts.shell.state :as state]
    [status-im.contexts.shell.utils :as utils]
    [utils.worklets.shell :as worklets.shell]))

(defn stacks-and-bottom-tabs-derived-values
  [selected-stack-id]
  (reduce
   (fn [acc id]
     (let [tabs-icon-color-keyword (get shell.constants/tabs-icon-color-keywords id)
           stack-opacity-keyword   (get shell.constants/stacks-opacity-keywords id)
           stack-z-index-keyword   (get shell.constants/stacks-z-index-keywords id)]
       (assoc
        acc
        stack-opacity-keyword
        (worklets.shell/stack-opacity (name id) selected-stack-id)
        stack-z-index-keyword
        (worklets.shell/stack-z-index (name id) selected-stack-id)
        tabs-icon-color-keyword
        (worklets.shell/bottom-tab-icon-color
         (name id)
         selected-stack-id
         colors/white
         colors/neutral-50))))
   {}
   shell.constants/stacks-ids))

(defn calculate-and-set-shared-values
  []
  (let [selected-stack-id (reanimated/use-shared-value (name @state/selected-stack-id-value))]
    (utils/reset-bottom-tabs) ;; Reset the state of loaded tabs for faster re-login and hot reloads
    (reset! state/selected-stack-id-shared-value selected-stack-id)
    (stacks-and-bottom-tabs-derived-values selected-stack-id)))
