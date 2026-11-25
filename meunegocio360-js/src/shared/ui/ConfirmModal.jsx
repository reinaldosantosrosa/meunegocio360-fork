import { Button, Modal, Spinner } from 'react-bootstrap'

export function ConfirmModal({
  show,
  title,
  children,
  onConfirm,
  onCancel,
  confirmLabel = 'Confirmar',
  isPending = false,
  pendingLabel = 'Salvando...',
}) {
  return (
    <Modal
      show={show}
      onHide={isPending ? undefined : onCancel}
      centered
      aria-labelledby="confirm-modal-title"
    >
      <Modal.Header closeButton={!isPending}>
        <Modal.Title id="confirm-modal-title">{title}</Modal.Title>
      </Modal.Header>
      <Modal.Body>{children}</Modal.Body>
      <Modal.Footer>
        <Button
          type="button"
          variant="outline-dark"
          onClick={onCancel}
          disabled={isPending}
        >
          Cancelar
        </Button>
        <Button
          type="button"
          variant="primary"
          onClick={onConfirm}
          disabled={isPending}
        >
          {isPending ? (
            <>
              <Spinner size="sm" animation="border" aria-hidden="true" />{' '}
              {pendingLabel}
            </>
          ) : (
            confirmLabel
          )}
        </Button>
      </Modal.Footer>
    </Modal>
  )
}
