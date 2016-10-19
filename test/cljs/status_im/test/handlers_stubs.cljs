(ns status-im.test.handlers-stubs
  (:require [re-frame.core :refer [subscribe dispatch after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.utils.handlers :as u]
            [status-im.chat.sign-up :as sign-up-service]
            status-im.handlers))

(defn init-stubs []
  (register-handler :sign-up
    (fn []
      ;; todo save phone number to db
      (sign-up-service/on-sign-up-response)))

  (register-handler :sign-up-confirm
    (u/side-effect!
      (fn [_ [_ confirmation-code]]
        (sign-up-service/on-send-code-response
          (if (= "1234" confirmation-code)
            {:message   "Done!"
             :status    :confirmed
             :confirmed true}
            {:message   "Wrong code!"
             :status    :failed
             :confirmed false}))))))
