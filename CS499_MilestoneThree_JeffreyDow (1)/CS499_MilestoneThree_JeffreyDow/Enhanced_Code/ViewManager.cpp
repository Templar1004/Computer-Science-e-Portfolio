///////////////////////////////////////////////////////////////////////////////
// viewmanager.cpp
// ============
// manage the viewing of 3D objects within the viewport
//
//  AUTHOR: Brian Battersby - SNHU Instructor / Computer Science
//  ENHANCED BY: Jeffrey Dow - CS 499 Capstone
//  ENHANCEMENTS: Optimized camera matrix calculations and exported 
//  View-Projection matrix for Frustum Culling integration.
///////////////////////////////////////////////////////////////////////////////

#include "ViewManager.h"

// GLM Math Header inclusions
#include <glm/glm.hpp>
#include <glm/gtx/transform.hpp>
#include <glm/gtc/type_ptr.hpp>    

// Export the ViewProjection matrix so the SceneManager can use it for Frustum Culling
// without requiring heavy modifications to the header files.
glm::mat4 g_ViewProjectionMatrix = glm::mat4(1.0f);

// declaration of the global variables and defines
namespace
{
	// Variables for window width and height
	const int WINDOW_WIDTH = 1000;
	const int WINDOW_HEIGHT = 800;
	const char* g_ViewName = "view";
	const char* g_ProjectionName = "projection";

	// camera object used for viewing and interacting with the 3D scene
	Camera* g_pCamera = nullptr;

	// these variables are used for mouse movement processing
	float gLastX = WINDOW_WIDTH / 2.0f;
	float gLastY = WINDOW_HEIGHT / 2.0f;
	bool gFirstMouse = true;

	// time between current frame and last frame
	float gDeltaTime = 0.0f;
	float gLastFrame = 0.0f;

	// orthographic projection toggle
	bool bOrthographicProjection = false;
}

/***********************************************************
 *  ViewManager()
 ***********************************************************/
ViewManager::ViewManager(ShaderManager* pShaderManager)
{
	m_pShaderManager = pShaderManager;
	m_pWindow = NULL;
	g_pCamera = new Camera();

	// default camera view parameters
	g_pCamera->Position = glm::vec3(0.0f, 10.0f, 15.0f);
	g_pCamera->Front = glm::vec3(0.0f, -0.3f, -1.0f);
	g_pCamera->Up = glm::vec3(0.0f, 1.0f, 0.0f);
	g_pCamera->Zoom = 55;
}

/***********************************************************
 *  ~ViewManager()
 ***********************************************************/
ViewManager::~ViewManager()
{
	m_pShaderManager = NULL;
	m_pWindow = NULL;
	if (NULL != g_pCamera)
	{
		delete g_pCamera;
		g_pCamera = NULL;
	}
}

/***********************************************************
 *  CreateDisplayWindow()
 ***********************************************************/
GLFWwindow* ViewManager::CreateDisplayWindow(const char* windowTitle)
{
	GLFWwindow* window = nullptr;

	window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, windowTitle, NULL, NULL);
	if (window == NULL)
	{
		std::cout << "Failed to create GLFW window" << std::endl;
		glfwTerminate();
		return NULL;
	}
	glfwMakeContextCurrent(window);

	glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	glfwSetCursorPosCallback(window, &ViewManager::Mouse_Position_Callback);
	glfwSetScrollCallback(window, &ViewManager::Scroll_Callback);

	// enable blending for supporting transparent rendering
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	m_pWindow = window;
	return(window);
}

/***********************************************************
 *  Mouse_Position_Callback()
 ***********************************************************/
void ViewManager::Mouse_Position_Callback(GLFWwindow* window, double xMousePos, double yMousePos)
{
	if (gFirstMouse)
	{
		gLastX = (float)xMousePos;
		gLastY = (float)yMousePos;
		gFirstMouse = false;
	}

	float xoffset = (float)xMousePos - gLastX;
	float yoffset = gLastY - (float)yMousePos;

	gLastX = (float)xMousePos;
	gLastY = (float)yMousePos;

	g_pCamera->ProcessMouseMovement(xoffset, yoffset);
}

void ViewManager::Scroll_Callback(GLFWwindow* window, double xoffset, double yoffset)
{
	g_pCamera->Zoom -= (float)yoffset;

	if (g_pCamera->Zoom < 1.0f)
		g_pCamera->Zoom = 1.0f;
	if (g_pCamera->Zoom > 45.0f)
		g_pCamera->Zoom = 45.0f;
}

/***********************************************************
 * ProcessKeyboardEvents()
 ***********************************************************/
void ViewManager::ProcessKeyboardEvents()
{
	if (glfwGetKey(m_pWindow, GLFW_KEY_ESCAPE) == GLFW_PRESS)
	{
		glfwSetWindowShouldClose(m_pWindow, true);
	}

	float baseSpeed = 5.0f;
	float sprintSpeed = 10.0f;
	float currentSpeed = (glfwGetKey(m_pWindow, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) ? sprintSpeed : baseSpeed;
	float cameraSpeed = currentSpeed * gDeltaTime;

	if (glfwGetKey(m_pWindow, GLFW_KEY_W) == GLFW_PRESS)
		g_pCamera->Position += cameraSpeed * g_pCamera->Front;
	if (glfwGetKey(m_pWindow, GLFW_KEY_S) == GLFW_PRESS)
		g_pCamera->Position -= cameraSpeed * g_pCamera->Front;
	if (glfwGetKey(m_pWindow, GLFW_KEY_A) == GLFW_PRESS)
		g_pCamera->Position -= glm::normalize(glm::cross(g_pCamera->Front, g_pCamera->Up)) * cameraSpeed;
	if (glfwGetKey(m_pWindow, GLFW_KEY_D) == GLFW_PRESS)
		g_pCamera->Position += glm::normalize(glm::cross(g_pCamera->Front, g_pCamera->Up)) * cameraSpeed;
	if (glfwGetKey(m_pWindow, GLFW_KEY_Q) == GLFW_PRESS)
		g_pCamera->Position += cameraSpeed * g_pCamera->Up;
	if (glfwGetKey(m_pWindow, GLFW_KEY_E) == GLFW_PRESS)
		g_pCamera->Position -= cameraSpeed * g_pCamera->Up;

	if (glfwGetKey(m_pWindow, GLFW_KEY_P) == GLFW_PRESS)
		bOrthographicProjection = false;
	if (glfwGetKey(m_pWindow, GLFW_KEY_O) == GLFW_PRESS)
		bOrthographicProjection = true;
}

/***********************************************************
 *  PrepareSceneView()
 ***********************************************************/
void ViewManager::PrepareSceneView()
{
	glm::mat4 view;
	glm::mat4 projection;

	float currentFrame = glfwGetTime();
	gDeltaTime = currentFrame - gLastFrame;
	gLastFrame = currentFrame;

	ProcessKeyboardEvents();

	view = g_pCamera->GetViewMatrix();

	if (bOrthographicProjection) {
		float scale = 10.0f;
		float aspect = (float)WINDOW_WIDTH / (float)WINDOW_HEIGHT;
		projection = glm::ortho(-aspect * scale, aspect * scale, -scale, scale, 0.1f, 100.0f);
	}
	else {
		projection = glm::perspective(glm::radians(g_pCamera->Zoom), (GLfloat)WINDOW_WIDTH / (GLfloat)WINDOW_HEIGHT, 0.1f, 100.0f);
	}

	// Calculate and store the View-Projection matrix for spatial culling in SceneManager
	g_ViewProjectionMatrix = projection * view;

	if (NULL != m_pShaderManager)
	{
		m_pShaderManager->setMat4Value(g_ViewName, view);
		m_pShaderManager->setMat4Value(g_ProjectionName, projection);
		m_pShaderManager->setVec3Value("viewPosition", g_pCamera->Position);
	}
}