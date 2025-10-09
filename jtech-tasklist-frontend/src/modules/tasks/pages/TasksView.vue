<template>
  <v-container>
    <v-row class="align-center mb-4 task-header">
      <v-col cols="auto">
        <v-btn @click="router.push({ name: 'lists' })"><i class="bi bi-arrow-left"></i></v-btn>
      </v-col>
      <v-col cols="6"><h2 class="list-title">{{ listName || 'Tarefas' }}</h2></v-col>
      <v-col cols="4" class="text-right action-buttons">
        <v-btn color="primary" @click="showCreateDialog = true">Nova Tarefa</v-btn>
        <v-btn color="error" @click="deleteList" class="ml-2">Excluir Lista</v-btn>
      </v-col>
    </v-row>

    <v-list>
      <v-list-item v-for="t in tasks.items" :key="t.id" class="task-list-item">
        <template #prepend>
          <v-btn icon variant="text" @click.stop="toggle(t)">
            <i class="bi" :class="t.completed ? 'bi-check-square-fill' : 'bi-square'"></i>
          </v-btn>
        </template>
        <template #title>
          <div class="task-title" :style="{ textDecoration: t.completed ? 'line-through' : 'none' }">
            {{ t.title }}
          </div>
        </template>
        <template #subtitle>
          <div v-if="t.description" class="task-description" :style="{ textDecoration: t.completed ? 'line-through' : 'none' }">
            <strong>Descrição:</strong> {{ t.description }}
          </div>
          <div v-if="t.dueDate" class="task-due-date" :style="{ textDecoration: t.completed ? 'line-through' : 'none' }">
            <strong>Data de Vencimento:</strong> {{ formatDate(t.dueDate) }}
          </div>
        </template>
        <template #append>
          <v-btn variant="text" size="x-small" @click="view(t)">Ver Detalhes</v-btn>
          <v-btn variant="text" size="x-small" @click="edit(t)">Editar</v-btn>
          <v-btn variant="text" size="x-small" color="error" @click="remove(t.id)">Excluir</v-btn>
        </template>
      </v-list-item>
    </v-list>

    <v-dialog v-model="showCreateDialog" max-width="500px" class="create-dialog">
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

    <v-dialog v-model="showViewDialog" max-width="500px">
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

    <v-dialog v-model="showEditDialog" max-width="500px">
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
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useTasksStore } from '@/stores/useTasksStore'
import { useTasklistsStore } from '@/stores/useTasklistsStore'
import { useRoute, useRouter } from 'vue-router'
import type { Task } from '@/core/models/task'

const tasks = useTasksStore()
const lists = useTasklistsStore()
const route = useRoute()
const router = useRouter()
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
    alert('Já existe uma tarefa com esse nome nesta lista.')
    return
  }
  await tasks.create(route.params.listId as string, title, newDescription.value, newDueDate.value)
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
const remove = async (id: string) => { if (confirm('Excluir tarefa?')) await tasks.remove(id) }
const deleteList = async () => {
  if (confirm('Excluir esta lista e suas tarefas?')) {
    await lists.remove(route.params.listId as string)
    router.push({ name: 'lists' })
  }
}
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
    alert('Já existe uma tarefa com esse nome nesta lista.')
    return
  }
  await tasks.update({ ...selectedTask.value, title, description: editDescription.value, dueDate: editDueDate.value })
  showEditDialog.value = false
}
</script>

<style scoped>
.task-header {
  /* Adicione seus estilos para o cabeçalho da tarefa aqui */
}

.back-button {
  /* Estilos para o botão de voltar */
}

.list-title {
  /* Estilos para o título da lista */
}

.action-buttons {
  /* Estilos para os botões de ação (Nova Tarefa, Excluir Lista) */
}

.task-list-item {
  /* Estilos para os itens da lista de tarefas */
}

.task-title {
  /* Estilos para o título da tarefa */
}

.create-dialog {
  /* Estilos para o diálogo de criação de nova tarefa */
}
</style>
