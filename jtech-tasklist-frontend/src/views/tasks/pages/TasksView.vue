<template>
  <v-container fluid class="pa-2 pa-sm-4 task-container" style="max-width: 900px; margin: 0 auto;">
    <v-row class="align-center mb-3 task-header">
      <v-col cols="auto" class="d-flex align-center">
        <v-btn @click="router.push({ name: 'lists' })" icon class="back-btn-mobile mr-2">
          <i class="bi bi-arrow-left"></i>
        </v-btn>
      </v-col>
      <v-col cols="12" sm="7" class="d-flex align-center justify-center justify-sm-start">
        <h2 class="list-title text-h6 text-sm-h5 text-truncate">{{ listName || 'Tarefas' }}</h2>
      </v-col>
      <v-col cols="12" sm="5" class="action-buttons d-flex flex-sm-row flex-column align-center align-sm-end gap-2 mt-2 mt-sm-0">
        <v-btn color="primary" @click="showCreateDialog = true" block class="mb-1 mb-sm-0">
          <i class="bi bi-plus-circle mr-1"></i> Nova Tarefa
        </v-btn>
        <v-btn color="error" @click="showDeleteListDialog = true" block>
          <i class="bi bi-trash mr-1"></i> Excluir Lista
        </v-btn>
      </v-col>
    </v-row>

    <v-row class="g-2">
      <v-col cols="12" v-for="t in tasks.items" :key="t.id">
        <v-card class="task-card d-flex flex-column flex-sm-row align-center pa-3 mb-3">
          <div class="d-flex align-center mr-3 mb-2 mb-sm-0">
            <v-btn icon variant="text" @click.stop="toggle(t)">
              <i class="bi" :class="t.completed ? 'bi-check-square-fill' : 'bi-square'" style="font-size: 1.3rem;"></i>
            </v-btn>
          </div>
          <div class="flex-grow-1">
            <div class="task-title text-truncate mb-1" :style="{ textDecoration: t.completed ? 'line-through' : 'none' }">
              {{ t.title }}
            </div>
            <div v-if="t.description" class="task-description text-truncate mb-1" :style="{ textDecoration: t.completed ? 'line-through' : 'none' }">
              <i class="bi bi-card-text mr-1"></i> {{ t.description }}
            </div>
            <div v-if="t.dueDate" class="task-due-date text-truncate" :style="{ textDecoration: t.completed ? 'line-through' : 'none' }">
              <i class="bi bi-calendar-event mr-1"></i> {{ formatDate(t.dueDate) }}
            </div>
          </div>
          <div class="d-flex flex-row flex-wrap gap-1 mt-2 mt-sm-0 ml-sm-3">
            <v-btn variant="text" size="small" @click="view(t)"><i class="bi bi-eye"></i></v-btn>
            <v-btn variant="text" size="small" @click="edit(t)"><i class="bi bi-pencil"></i></v-btn>
            <v-btn variant="text" size="small" color="error" @click="remove(t.id)"><i class="bi bi-trash"></i></v-btn>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <v-dialog v-model="showCreateDialog" max-width="420px" min-width="260px">
      <v-card>
        <v-card-title>Nova Tarefa</v-card-title>
        <v-card-text>
          <v-text-field v-model="newTitle" label="Título" :rules="[v => !!v || 'Título é obrigatório']" required />
          <v-textarea v-model="newDescription" label="Descrição" rows="3" />
          <v-text-field v-model="newDueDate" label="Data de Vencimento" type="date" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showCreateDialog = false">Cancelar</v-btn>
          <v-btn color="primary" @click="create">Criar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showViewDialog" max-width="420px" min-width="260px">
      <v-card>
        <v-card-title>Detalhes da Tarefa</v-card-title>
        <v-card-text>
          <p><strong>Título:</strong> {{ selectedTask?.title }}</p>
          <p v-if="selectedTask?.description"><strong>Descrição:</strong> {{ selectedTask.description }}</p>
          <p v-if="selectedTask?.dueDate"><strong>Data de Vencimento:</strong> {{ formatDate(selectedTask.dueDate) }}</p>
          <p><strong>Status:</strong> {{ selectedTask?.completed ? 'Concluída' : 'Pendente' }}</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showViewDialog = false">Fechar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showEditDialog" max-width="420px" min-width="260px">
      <v-card>
        <v-card-title>Editar Tarefa</v-card-title>
        <v-card-text>
          <v-text-field v-model="editTitle" label="Título" required />
          <v-textarea v-model="editDescription" label="Descrição" rows="3" />
          <v-text-field v-model="editDueDate" label="Data de Vencimento" type="date" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showEditDialog = false">Cancelar</v-btn>
          <v-btn color="primary" @click="saveEdit">Salvar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showDeleteTaskDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h6">Confirmar Exclusão</v-card-title>
        <v-card-text>Tem certeza que deseja excluir esta tarefa?</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showDeleteTaskDialog = false">Cancelar</v-btn>
          <v-btn color="error" @click="confirmDeleteTask">Excluir</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showDeleteListDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h6">Confirmar Exclusão</v-card-title>
        <v-card-text>Tem certeza que deseja excluir esta lista e suas tarefas?</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showDeleteListDialog = false">Cancelar</v-btn>
          <v-btn color="error" @click="confirmDeleteList">Excluir</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useTasksStore } from '@/stores/useTasksStore'
import { useTasklistsStore } from '@/stores/useTasklistsStore'
import { useRoute, useRouter } from 'vue-router'
import { useUiStore } from '@/stores/useUiStore'
import type { Task } from '@/core/models/task'

const tasks = useTasksStore()
const lists = useTasklistsStore()
const route = useRoute()
const router = useRouter()
const ui = useUiStore()
const showCreateDialog = ref(false)
const newTitle = ref('')
const newDescription = ref('')
const newDueDate = ref('')

const showViewDialog = ref(false)
const selectedTask = ref<Task | null>(null)

const showEditDialog = ref(false)
const editTitle = ref('')
const editDescription = ref('')
const editDueDate = ref('')

const showDeleteTaskDialog = ref(false)
const showDeleteListDialog = ref(false)

const listName = computed(() => lists.items.find(l => l.id === route.params.listId)?.name)

const load = async () => {
  await tasks.fetch(route.params.listId as string)
  if (!lists.items.length) await lists.fetch()
}
onMounted(load)
watch(() => route.params.listId, load)

const create = async () => {
  if (!newTitle.value.trim()) return
  const title = newTitle.value.trim()
  if (tasks.items.some(t => t.title.toLowerCase() === title.toLowerCase())) {
    ui.showSnackbar('Já existe uma tarefa com esse nome nesta lista.', 'error')
    return
  }
  await tasks.create(route.params.listId as string, title, newDescription.value, newDueDate.value)
  ui.showSnackbar('Tarefa criada com sucesso!', 'success')
  newTitle.value = ''
  newDescription.value = ''
  newDueDate.value = ''
  showCreateDialog.value = false
}
const toggle = async (t: Task) => { await tasks.toggle(t.id, !t.completed) }
const edit = (t: Task) => {
  selectedTask.value = t
  editTitle.value = t.title
  editDescription.value = t.description || ''
  editDueDate.value = t.dueDate || ''
  showEditDialog.value = true
}
const remove = (id: string) => {
  selectedTask.value = tasks.items.find(t => t.id === id) || null
  showDeleteTaskDialog.value = true
}
const deleteList = () => { showDeleteListDialog.value = true }
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('pt-BR')
}
const view = (task: Task) => {
  selectedTask.value = task
  showViewDialog.value = true
}
const saveEdit = async () => {
  if (!editTitle.value.trim()) return
  const title = editTitle.value.trim()
  if (tasks.items.some(t => t.id !== selectedTask.value?.id && t.title.toLowerCase() === title.toLowerCase())) {
    ui.showSnackbar('Já existe uma tarefa com esse nome nesta lista.', 'error')
    return
  }
  await tasks.update({ ...(selectedTask.value as Task), title, description: editDescription.value, dueDate: editDueDate.value })
  ui.showSnackbar('Tarefa atualizada com sucesso!', 'success')
  showEditDialog.value = false
}
const confirmDeleteTask = async () => {
  if (selectedTask.value) {
    await tasks.remove(selectedTask.value.id)
    ui.showSnackbar('Tarefa excluída com sucesso!', 'success')
    showDeleteTaskDialog.value = false
  }
}
const confirmDeleteList = async () => {
  await lists.remove(route.params.listId as string)
  ui.showSnackbar('Lista excluída com sucesso!', 'success')
  router.push({ name: 'lists' })
}
</script>

<style scoped>
.task-container {
  background: #f5f6fa;
  min-height: 100vh;
}
.task-header {
  margin-bottom: 1rem;
}
.list-title {
  font-weight: 600;
  letter-spacing: 0.5px;
}
.action-buttons .v-btn {
  min-width: 120px;
}
.task-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: box-shadow 0.2s;
}
.task-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.10);
}
.task-title {
  font-size: 1.08rem;
  font-weight: 500;
  color: #222;
}
.task-description, .task-due-date {
  font-size: 0.97rem;
  color: #555;
}
@media (max-width: 600px) {
  .task-header {
    flex-direction: column;
    align-items: stretch !important;
    margin-bottom: 0.5rem;
  }
  .list-title {
    font-size: 1.1rem !important;
    margin-bottom: 0.5rem;
    text-align: center;
  }
  .action-buttons {
    flex-direction: column !important;
    align-items: stretch !important;
    gap: 0.5rem;
  }
  .action-buttons .v-btn {
    width: 100%;
    min-width: unset;
    margin-left: 0 !important;
  }
  .task-card {
    padding: 1rem 0.7rem !important;
  }
  .task-title, .task-description, .task-due-date {
    font-size: 1rem !important;
  }
  .back-btn-mobile {
    margin-bottom: 0.5rem;
  }
}
</style>
