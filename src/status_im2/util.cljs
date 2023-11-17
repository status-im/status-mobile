(ns status-im2.util
  (:require-macros [status-im2.util :as um])
  (:require [re-frame.core :as rf]))

(defn do-on* [target-event callback]
  (let [cb-id (gensym "do-on-cb-fn")]
    (rf/add-post-event-callback
      cb-id
      (fn [event _]
        (when (= event target-event)
          (rf/remove-post-event-callback cb-id)
          (callback))))
    :ok))

(comment

  ;; Note: those examples do not work for the time being since stepping events
  ;; :hide-bottom-sheet and :navigate-to-within-stack seem to be sent synchronously
  ;; with the parent event through an fx effect.
  ;; A better scenario with trully asynchronous steps should work.
  ;; The following examples are here to expose the syntax of the um/do-on and
  ;; um/run-scenario macros.

  ;; Nested example
  (do
    (when-let [blur-show-fn @status-im2.contexts.onboarding.common.overlay.view/blur-show-fn-atom]
      (blur-show-fn))
    (rf/dispatch [:open-modal :new-to-status])
    (um/do-on :hide-bottom-sheet
      (rf/dispatch [:onboarding-2/navigate-to-create-profile])
      (um/do-on :navigate-to-within-stack
        (rf/dispatch [:onboarding-2/profile-data-set
                      {:image-path nil, :display-name "lambdam", :color :blue}]))))

  ;; "Aligned" example (which expands to the same code than the previous example)
  (um/run-scenario
    (when-let [blur-show-fn @status-im2.contexts.onboarding.common.overlay.view/blur-show-fn-atom]
      (blur-show-fn))
    (rf/dispatch [:open-modal :new-to-status])
    [:on :hide-bottom-sheet]
    (rf/dispatch [:onboarding-2/navigate-to-create-profile])
    [:on :navigate-to-within-stack]
    (rf/dispatch [:onboarding-2/profile-data-set
                  {:image-path nil, :display-name "lambdam", :color :blue}]))

  )
