(ns status-im.contexts.shell.share.events
  (:require
    [status-im.common.toasts.events :as toasts]
    [utils.re-frame :as rf]))

(rf/defn copy-text-and-show-toast
  {:events [:share/copy-text-and-show-toast]}
  [cofx {:keys [text-to-copy post-copy-message]}]
  (rf/merge cofx
            {:copy-to-clipboard text-to-copy}
            (toasts/upsert
             {:icon  :i/correct
              :id    :successful-copy-toast-message
              :type  :positive
              :theme :dark
              :text  post-copy-message})))
